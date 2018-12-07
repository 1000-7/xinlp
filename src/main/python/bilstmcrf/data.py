import logging
import random

import tflearn

import config
from embedding import get_embedding

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

tag2label = {"B": 0, "E": 1, "M": 2, "S": 3}


def read_corpus(random, max_len):
    vocab, word2id, embedding = get_embedding(random, 300)
    sentsid_, sents_, tags_ = [], [], []
    logging.info("开始读取数据集")
    with open(config.split_data, encoding='utf-8') as fr:
        lines = fr.readlines()
    sentid_, sent_, tag_ = [], [], []
    for line in lines:
        if line != '\n':
            char, label = line.strip().split()
            tag_.append(tag2label[label])
            if char.startswith("num"):
                sent_.append("num")
                sentid_.append(1)
            elif char.startswith("en"):
                sent_.append("en")
                sentid_.append(2)
            elif '\u4e00' <= char <= '\u9fa5' and char in vocab:
                sent_.append(char)
                sentid_.append(word2id[char])
            else:
                sent_.append("unk")
                sentid_.append(0)
        else:
            if 3 < len(sent_) <= max_len:
                sents_.append(sent_)
                tags_.append(tag_)
                sentsid_.append(sentid_)
                sentid_, sent_, tag_ = [], [], []
            else:
                sentid_, sent_, tag_ = [], [], []
    padding_tags = tflearn.data_utils.pad_sequences(tags_, maxlen=max_len, value=3)
    padding_sentsid = tflearn.data_utils.pad_sequences(sentsid_, maxlen=max_len, value=0)
    # print(sents_[0])
    # print(padding_sentsid[0])
    # print(padding_tags[0])
    return padding_sentsid, sents_, padding_tags


def sentence2id(sent, word2id):
    sentid_ = []
    for char in sent:
        if char.startswith("num"):
            sentid_.append(1)
        elif char.startswith("en"):
            sentid_.append(2)
        elif '\u4e00' <= char <= '\u9fa5' and char in word2id.keys():
            sentid_.append(word2id[char])
        else:
            sentid_.append(0)
    return sentid_


# 主要是为了打乱顺序
def get_train_test_data(embedding_random, max_len):
    data = []
    train_data = []
    test_data = []
    padding_sentsid, sents_, padding_tags = read_corpus(embedding_random, max_len)
    l = len(sents_)
    for i in range(l):
        data.append((padding_sentsid[i], padding_tags[i]))
    random.shuffle(data)

    for i in range(l):
        (sentid_, tag_) = data[i]
        if i < 0.9 * l:
            train_data.append((sentid_, tag_))
        else:
            test_data.append((sentid_, tag_))

    return train_data, test_data


def batch_yield(data, batch_size):
    random.shuffle(data)
    seqs, labels = [], []
    for (sentid_, tag_) in data:
        if len(seqs) == batch_size:
            yield seqs, labels
            seqs, labels = [], []
        seqs.append(sentid_)
        labels.append(tag_)

# train_x, train_y, test_x, test_y = get_train_test_data(50)
# print(train_y[0])
# print(train_x[0])
# print(test_y[0])
# print(test_x[0])
