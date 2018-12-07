import logging
import re
import numpy as np
import gensim

model = gensim.models.Word2Vec.load('/media/wangxin/data/w2v2.bin')
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)


def tag(str):
    if str == 'B':
        return 0
    elif str == 'E':
        return 1
    elif str == 'M':
        return 2
    else:
        return 3


def get_idlist_vec(ids, id_word):
    idlist_vec = []
    for id in ids:
        idlist_vec.append(id_word[id])
    return idlist_vec


def get_batch_idlist_vec(batchids, id_word):
    batch_idlist_vec = []
    for ids in batchids:
        batch_idlist_vec.append(get_idlist_vec(ids, id_word))
    return batch_idlist_vec


def get_basedata(filename, batch_size, maxlen=30):
    logging.info("正在读取相关数据")
    f = open(filename, 'r')
    word_set = set()
    word_id = {}
    id_word = {}
    id_vec = {}

    sentence_list_list = []
    id = 0
    sentence_list = []
    sentence = []
    tags_list_list = []
    tags_list = []
    tags = []

    size = 0
    for line in f:
        line = re.sub("[^\u4e00-\u9fa5A-Z\s]", "", line)
        split = line.split(" ")
        if line == '\n' or line == '' or split[0] == '':
            curl = len(sentence)
            if 2 < curl < maxlen:
                for x in range(len(tags), maxlen):
                    tags.append(3)
                sentence_list.append(sentence)
                tags_list.append(tags)
                sentence = []
                tags = []
                size += 1
                if size == batch_size:
                    sentence_list_list.append(sentence_list)
                    tags_list_list.append(tags_list)
                    sentence_list = []
                    tags_list = []
                    size = 0
            else:
                sentence = []
                tags = []
            continue
        # print(line)

        if split[0] != "" and split[0] != '\n':
            sentence.append(split[0])
            # print(split[1].replace('\n', ''))
            tags.append(tag(split[1].replace('\n', '')))

        if split[0] == "" or split[0].startswith("num") or split[0].startswith("en"):
            continue

        # print(split[0] + " " + split[1])
        # if split[0] not in word_set and split[0] in model:
        if split[0] not in word_set:
            word_set.add(split[0])
            word_id[split[0]] = id
            id_word[id] = split[0]
            # id_vec[id] = model[split[0]]
            id += 1
        word_set.add('<UNK>')
        id_word[5543] = '<UNK>'
        word_id['<UNK>'] = 5543
        # id_vec[5543] = model['鶨']

    logging.info("训练数据中出现的字一共有：")
    logging.info(len(word_set))
    logging.info("读取完毕")
    return word_set, word_id, id_word, id_vec, sentence_list_list, tags_list_list


def get_ids_list_list(sentence_list_list, word_id, maxlen):
    ids_list_list = []
    for sentence_list in sentence_list_list:
        ids_list = []
        for sentence in sentence_list:
            ids = []
            for x in sentence:
                ids.append(word_id.get(x))
            for x in range(len(ids), maxlen):
                ids.append(5543)
            ids_list.append(ids)
        ids_list_list.append(ids_list)
    return ids_list_list


def get_sequence_lengths(sentence_list_list):
    sequence_lengths_batch = []
    for sentence_list in sentence_list_list:
        sequence_lengths = []
        for sentence in sentence_list:
            sequence_lengths.append(len(sentence))
        sequence_lengths_batch.append(sequence_lengths)
    return sequence_lengths_batch


def textpre(filepath, batch_size, maxlen):
    word_set, word_id, id_word, id_vec, sentence_list_list, tags_list_list = get_basedata(filepath, batch_size, maxlen)
    ids_list_list = get_ids_list_list(sentence_list_list, word_id, maxlen)
    sequence_lengths_batch = get_sequence_lengths(ids_list_list)
    logging.info("基本信息打印......")
    logging.info(sentence_list_list[0])
    logging.info(len(sentence_list_list))
    logging.info(len(sentence_list_list[0]))
    logging.info(tags_list_list[0])
    logging.info(len(tags_list_list))
    logging.info(len(tags_list_list[0]))
    logging.info(ids_list_list[0])
    logging.info(len(ids_list_list))
    logging.info(len(ids_list_list[0]))
    logging.info(sequence_lengths_batch[0])
    logging.info(len(sequence_lengths_batch))
    logging.info(len(sequence_lengths_batch[0]))

    logging.info("所有需要的数据已经拿到")

    return word_set, word_id, id_word, id_vec, sentence_list_list, ids_list_list, sequence_lengths_batch, tags_list_list


def random_embedding(vocab_size, embedding_dim):
    embedding_mat = np.random.uniform(-0.25, 0.25, (vocab_size, embedding_dim))
    embedding_mat = np.float32(embedding_mat)
    return embedding_mat

def pre_embedding(id_vector):
    embedding = list(id_vector.values())
    embedding_np = np.array(embedding)
    return embedding_np


# TODO:优雅的进行句子长度的padding

def str2id(str,word_id):
    ids=[]
    for s in str:
        ids.append(word_id.get(s))
    print(ids)

# word_set, word_id, id_word, id_vec, sentence_list_list, ids_list_list, sequence_lengths_batch, tags_list_list = textpre(
#     "/home/wangxin/CRF++-0.58/example/test/train.txt", 64, 20)
# str2id("我今天很高兴",word_id)


