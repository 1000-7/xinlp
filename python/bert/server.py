import collections
import os

import six
import tensorflow as tf
from bert import modeling
from gevent import monkey

monkey.patch_all()
from flask import Flask, request
from gevent import pywsgi
import numpy as np
import json

flags = tf.flags

FLAGS = flags.FLAGS

bert_path = '/Users/unclewang/.xinlp/data/chinese_L-12_H-768_A-12'

flags.DEFINE_string(
    "bert_config_file", os.path.join(bert_path, 'bert_config.json'),
    "The config json file corresponding to the pre-trained BERT model."
)

flags.DEFINE_string(
    "bert_vocab_file", os.path.join(bert_path, 'vocab.txt'),
    "The config vocab file"
)

flags.DEFINE_string(
    "init_checkpoint", os.path.join(bert_path, 'bert_model.ckpt'),
    "Initial checkpoint (usually from a pre-trained BERT model)."
)

app = Flask(__name__)


def convert_to_unicode(text):
    """Converts `text` to Unicode (if it's not already), assuming utf-8 input."""
    if six.PY3:
        if isinstance(text, str):
            return text
        elif isinstance(text, bytes):
            return text.decode("utf-8", "ignore")
        else:
            raise ValueError("Unsupported string type: %s" % (type(text)))
    elif six.PY2:
        if isinstance(text, str):
            return text.decode("utf-8", "ignore")
        elif isinstance(text, unicode):
            return text
        else:
            raise ValueError("Unsupported string type: %s" % (type(text)))
    else:
        raise ValueError("Not running on Python2 or Python 3?")


def load_vocab(vocab_file):
    vocab = collections.OrderedDict()
    vocab.setdefault("blank", 2)
    index = 0
    with tf.gfile.GFile(vocab_file, "r") as reader:
        while True:
            token = convert_to_unicode(reader.readline())
            if not token:
                break
            token = token.strip()
            vocab[token] = index
            index += 1
    return vocab


di = load_vocab(vocab_file=FLAGS.bert_vocab_file)
init_checkpoint = FLAGS.init_checkpoint
use_tpu = False

sess = tf.Session()

bert_config = modeling.BertConfig.from_json_file(FLAGS.bert_config_file)

print(init_checkpoint)

is_training = False
use_one_hot_embeddings = False


def inputs(vectors, maxlen=10):
    length = len(vectors)
    if length >= maxlen:
        return vectors[0:maxlen], [1] * maxlen, [0] * maxlen
    else:
        input = vectors + [0] * (maxlen - length)
        mask = [1] * length + [0] * (maxlen - length)
        segment = [0] * maxlen
        return input, mask, segment


input_ids_p = tf.placeholder(shape=[None, None], dtype=tf.int32, name="input_ids_p")
input_mask_p = tf.placeholder(shape=[None, None], dtype=tf.int32, name="input_mask_p")
segment_ids_p = tf.placeholder(shape=[None, None], dtype=tf.int32, name="segment_ids_p")

model = modeling.BertModel(
    config=bert_config,
    is_training=is_training,
    input_ids=input_ids_p,
    input_mask=input_mask_p,
    token_type_ids=segment_ids_p,
    use_one_hot_embeddings=use_one_hot_embeddings
)

restore_saver = tf.train.Saver()
restore_saver.restore(sess, init_checkpoint)


@app.route('/bert')
def response_request():
    text = request.args.get('text')

    vectors = [di.get("[CLS]")] + [di.get(i) if i in di else di.get("[UNK]") for i in list(text)] + [di.get("[SEP]")]

    input, mask, segment = inputs(vectors)

    input_ids = np.reshape(np.array(input), [1, -1])
    input_mask = np.reshape(np.array(mask), [1, -1])
    segment_ids = np.reshape(np.array(segment), [1, -1])

    embedding = tf.squeeze(model.get_sequence_output())

    ret = sess.run(embedding,
                   feed_dict={"input_ids_p:0": input_ids, "input_mask_p:0": input_mask, "segment_ids_p:0": segment_ids})
    return json.dumps(ret.tolist(), ensure_ascii=False)


if __name__ == "__main__":
    server = pywsgi.WSGIServer(('0.0.0.0', 19877), app)
    server.serve_forever()
