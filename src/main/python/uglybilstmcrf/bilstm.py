import numpy as np
import tensorflow as tf
from textpre import *

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
tag = ['B', 'E', 'M', 'S']
tag_num = 4
training_iters = 2000
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

# model parameter
learning_rate = 0.001
embedding_dim = 300
n_hidden_units = 128
batch_size = 64
max_sentence_length = 20
word_set, word_id, id_word, id_vec, sentence_list_list, ids_list_list, sequence_lengths_batch, tags_list_list = textpre(
    "/home/wangxin/CRF++-0.58/example/test/train.txt", batch_size, max_sentence_length)

embedding = random_embedding(len(word_set), embedding_dim)
# TODO:换成预训练的字嵌入在训练一遍
# embedding = pre_embedding(id_vector=id_vec)

batch_num = len(sentence_list_list)

# shape = (batch size)
sequence_lengths = tf.placeholder(tf.int32, shape=[batch_size])

x = tf.placeholder(tf.int32, shape=[batch_size, None])
# batch_size * 句子长度 每个标签都是固定的 注意是标签值所以都是tf.int32类型
y = tf.placeholder(tf.int32, shape=[batch_size, None])
word_embeddings = tf.Variable(embedding,
                              dtype=tf.float32,
                              trainable=True,
                              name="word_embeddings")
id_embeddings = tf.nn.embedding_lookup(params=word_embeddings,
                                       ids=x,
                                       name="id_embeddings")
with tf.variable_scope('softmax') as scope:
    w = tf.get_variable(shape=[n_hidden_units * 2, tag_num], initializer=tf.truncated_normal_initializer(stddev=0.1),
                        name="weights", regularizer=tf.contrib.layers.l2_regularizer(0.001))
    b = tf.get_variable(shape=[tag_num], name="bias")
cell_fw = tf.nn.rnn_cell.LSTMCell(num_units=n_hidden_units, forget_bias=1.0, state_is_tuple=True)
cell_bw = tf.nn.rnn_cell.LSTMCell(num_units=n_hidden_units, forget_bias=1.0, state_is_tuple=True)
init_state_fw = cell_fw.zero_state(batch_size, dtype=tf.float32)
init_state_bw = cell_bw.zero_state(batch_size, dtype=tf.float32)
# outputs, final_state = tf.nn.dynamic_rnn(lstm_cell, X_in, initial_state=init_state, time_major=False)
outputs, final_state = tf.nn.bidirectional_dynamic_rnn(cell_fw=cell_fw, cell_bw=cell_bw, inputs=id_embeddings,
                                                       sequence_length=sequence_lengths,
                                                       initial_state_fw=init_state_fw, initial_state_bw=init_state_bw,
                                                       time_major=False)
output_fw, output_bw = outputs
output = tf.concat([output_fw, output_bw], axis=-1)
matricized_output = tf.reshape(output, [-1, 2 * n_hidden_units])
output_scores = tf.matmul(matricized_output, w) + b
scores = tf.reshape(output_scores, [batch_size, max_sentence_length, tag_num])

# 计算log-likelihood并获得transition_params
log_likelihood, transition_params = tf.contrib.crf.crf_log_likelihood(scores, y, sequence_lengths)

losses = tf.nn.sparse_softmax_cross_entropy_with_logits(logits=scores, labels=y)
# shape = (batch, sentence, nclasses)
mask = tf.sequence_mask(sequence_lengths)
# apply mask
losses = tf.boolean_mask(losses, mask)

loss = tf.reduce_mean(losses)
optimizer = tf.train.AdamOptimizer(learning_rate)
train_op = optimizer.minimize(loss)
labels_pred = tf.cast(tf.argmax(scores, axis=-1), tf.int32)


def train():
    saver = tf.train.Saver()
    with tf.Session() as sess:
        sess.run(tf.global_variables_initializer())
        for epoch in range(training_iters):
            avg_cost = 0.
            print(batch_num)
            for i in range(batch_num):
                batch_x = ids_list_list[i]
                # batch_x = np.reshape(ids_list_list[i], [batch_size, max_sentence_length])
                batch_y = tags_list_list[i]
                batch_sequence_lengths = sequence_lengths_batch[i]

                _, c = sess.run([train_op, loss], feed_dict={

                    x: batch_x,
                    y: batch_y,
                    sequence_lengths: batch_sequence_lengths
                })
                if (i + 1) % 1000 == 0:
                    print(c)
                avg_cost += c / batch_num
            if (epoch + 1) % 5 == 0:
                saver.save(sess=sess, global_step=epoch,
                           save_path="/home/wangxin/PycharmProjects/tfboys/rnn/bilstm/modelpath/bilstm")

            print("Epoch:", '%04d' % (epoch + 1))
            print(avg_cost)


def pre():
    saver = tf.train.Saver()

    with tf.Session() as sess:
        saver.restore(sess, save_path="/home/wangxin/PycharmProjects/tfboys/rnn/bilstm/modelpath/bilstm")
        logits, trans_params = sess.run([scores, transition_params],
                                        feed_dict={x: ids_list_list[100], y: tags_list_list[0],
                                                   sequence_lengths: sequence_lengths_batch[0]})
       # keep only the valid steps
        viterbi_seq, viterbi_score = tf.contrib.crf.viterbi_decode(
                logits[0], trans_params)
        print(ids_list_list[100][0])
        print(tags_list_list[100][0])
        print(viterbi_seq)



# train()
pre()
