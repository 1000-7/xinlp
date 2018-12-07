import argparse
import os

import tensorflow as tf

import config
from data import get_train_test_data, tag2label
from embedding import get_embedding
from model import BiLSTM_CRF
from util import str2bool

parser = argparse.ArgumentParser(description='利用Bilstm+crf进行中文分词')
parser.add_argument('--batch_size', type=int, default=64, help='#minibatch的数量')
parser.add_argument('--epoch', type=int, default=40, help='#训练次数')
parser.add_argument('--hidden_dim', type=int, default=128, help='#Lstm里隐藏状态的维度')
parser.add_argument('--optimizer', type=str, default='Adam', help='Adam/Adadelta/Adagrad/RMSProp/Momentum/SGD')
parser.add_argument('--lr', type=float, default=0.001, help='学习率')
parser.add_argument('--update_embedding', type=str2bool, default=True, help='默认训练')
parser.add_argument('--embedding_dim', type=int, default=300, help='字嵌入的维度')
parser.add_argument('--dropout', type=float, default=0.5, help='dropout保留比例')
parser.add_argument('--useCRF', type=str2bool, default=True, help='是否使用CRF训练损失函数，默认是CRF，false是使用softmax')
parser.add_argument('--max_len', type=int, default=50, help='字嵌入的维度')
parser.add_argument('--mode', type=str, default='train', help='三种模式：train/test/predict')
parser.add_argument('--embedding_random', type=str, default=True,
                    help='使用随机的字嵌入（True）还是已经预训练好的（False），默认使用随机')

args = parser.parse_args()

train_data, test_data = get_train_test_data(args.embedding_random, args.max_len)
vocab, word2id, embeddings = get_embedding(args.embedding_random, args.embedding_dim)

configs = tf.ConfigProto()
configs.gpu_options.allow_growth = True
configs.gpu_options.per_process_gpu_memory_fraction = 0.2
# paths setting
paths = {}
output_path = config.output_path
if not os.path.exists(output_path):
    os.makedirs(output_path)
summary_path = os.path.join(output_path, "summaries")
paths['summary_path'] = summary_path
if not os.path.exists(summary_path):
    os.makedirs(summary_path)
model_path = os.path.join(output_path, "checkpoints/")
if not os.path.exists(model_path):
    os.makedirs(model_path)
ckpt_prefix = os.path.join(model_path, "model")
paths['model_path'] = ckpt_prefix
result_path = os.path.join(output_path, "results")
paths['result_path'] = result_path
if not os.path.exists(result_path):
    os.makedirs(result_path)
log_path = os.path.join(result_path, "log.txt")
paths['log_path'] = log_path

if args.mode == 'train':
    model = BiLSTM_CRF(args, embeddings, tag2label, word2id, paths, configs)
    model.build_graph()
    model.train(train=train_data, test=test_data)
