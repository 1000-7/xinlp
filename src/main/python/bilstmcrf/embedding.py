import logging
import os
import pickle

import numpy as np

import config

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)


def get_embedding(random, embedding_dim):
    if random:
        vocab, word2id, embedding = random_embedding(embedding_dim)
    else:
        vocab, word2id, embedding = pre_train_embedding(embedding_dim)
    logging.info("向量已经生成结束")
    # with open(os.path.join('.', 'data/word2id.pkl'), 'wb') as fw:
    #     pickle.dump(word2id, fw)
    return vocab, word2id, embedding


def random_embedding(embedding_dim):
    vocab, word2id = get_word2id()
    embedding_random = np.random.uniform(-0.1, 0.1, (len(vocab), embedding_dim))
    embedding_random = np.float32(embedding_random)
    return vocab, word2id, embedding_random


def get_word2id():
    word2id = {}
    vocab = []
    file = open(config.chars_embedding, 'r')
    # 先读一行是向量信息
    line = file.readline().strip()
    # 其实是字数加3
    logging.info("随机生成[字数，维度]：" + line)
    vocab.append("unk")
    word2id["unk"] = 0
    vocab.append("num")
    word2id["num"] = 1
    vocab.append("en")
    word2id["en"] = 2

    d = 3
    for line in file:
        row = line.strip().split(' ')
        vocab.append(row[0])
        word2id[row[0]] = d
        d += 1
    file.close()
    return vocab, word2id


def pre_train_embedding(embedding_dim):
    vocab = []
    embedding = []
    word2id = {}
    file = open(config.chars_embedding, 'r')
    # 先读一行是向量信息
    line = file.readline().strip()
    # 其实是字数加3
    logging.info("预训练词向量信息：" + line)
    vocab.append("unk")
    word2id["unk"] = 0
    embedding.append(np.random.uniform(-0.25, 0.25, embedding_dim))
    vocab.append("num")
    word2id["num"] = 1
    embedding.append(np.random.uniform(-0.25, 0.25, embedding_dim))
    vocab.append("en")
    word2id["en"] = 2
    embedding.append(np.random.uniform(-0.25, 0.25, embedding_dim))
    d = 3
    for line in file:
        row = line.strip().split(' ')
        vocab.append(row[0])
        embedding.append(np.float32(row[1:]))
        word2id[row[0]] = d
        d += 1
    file.close()
    return vocab, word2id, embedding
