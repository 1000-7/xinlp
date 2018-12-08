import os
import sys
import time

import tensorflow as tf

from data import batch_yield
from util import get_logger, generate_sequence_len, conlleval


class BiLSTM_CRF:

    def __init__(self, args, embeddings, tag2label, word2id, paths, config):
        self.batch_size = args.batch_size
        self.epoch_num = args.epoch
        self.hidden_dim = args.hidden_dim
        self.update_embedding = args.update_embedding
        self.dropout_keep_prob = args.dropout
        self.max_len = args.max_len
        self.optimizer = args.optimizer
        self.useCRF = args.useCRF
        self.lr = args.lr
        self.embeddings = embeddings
        self.tag2label = tag2label
        self.num_tags = len(tag2label)
        self.word2id = word2id
        self.model_path = paths['model_path']
        self.summary_path = paths['summary_path']
        self.logger = get_logger(paths['log_path'])
        self.result_path = paths['result_path']
        self.config = config

    def build_graph(self):
        self.add_placeholders()
        self.lookup_layer_op()
        self.biLSTM_layer_op()
        self.softmax_pred_op()
        self.loss_op()
        self.train_step_op()
        self.init_op()

    def add_placeholders(self):
        self.word_ids = tf.placeholder(tf.int32, shape=[None, self.max_len], name="word_ids")
        self.labels = tf.placeholder(tf.int32, shape=[None, self.max_len], name="labels")
        self.sequence_lengths = tf.placeholder(tf.int32, shape=[self.batch_size], name="sequence_lengths")

        self.dropout_pl = tf.placeholder(dtype=tf.float32, shape=[], name="dropout")
        self.lr_pl = tf.placeholder(dtype=tf.float32, shape=[], name="lr")

    def lookup_layer_op(self):
        with tf.variable_scope("words"):
            _word_embeddings = tf.Variable(self.embeddings,
                                           dtype=tf.float32,
                                           trainable=self.update_embedding,
                                           name="_word_embeddings")
            word_embeddings = tf.nn.embedding_lookup(params=_word_embeddings,
                                                     ids=self.word_ids,
                                                     name="word_embeddings")
        self.word_embeddings = tf.nn.dropout(word_embeddings, self.dropout_pl)

    def biLSTM_layer_op(self):
        with tf.variable_scope("bi-lstm"):
            cell_fw = tf.nn.rnn_cell.LSTMCell(num_units=self.hidden_dim, forget_bias=1.0, state_is_tuple=True)
            cell_bw = tf.nn.rnn_cell.LSTMCell(num_units=self.hidden_dim, forget_bias=1.0, state_is_tuple=True)
            (output_fw_seq, output_bw_seq), _ = tf.nn.bidirectional_dynamic_rnn(
                cell_fw=cell_fw,
                cell_bw=cell_bw,
                inputs=self.word_embeddings,
                sequence_length=self.sequence_lengths,
                dtype=tf.float32)
            output = tf.concat([output_fw_seq, output_bw_seq], axis=-1)
            output = tf.nn.dropout(output, self.dropout_pl)
        with tf.variable_scope("full-connect"):
            W = tf.get_variable(name="weights",
                                shape=[2 * self.hidden_dim, self.num_tags],
                                initializer=tf.truncated_normal_initializer(stddev=0.1),
                                dtype=tf.float32, regularizer=tf.contrib.layers.l2_regularizer(0.001))
            b = tf.get_variable(name="bias",
                                shape=[self.num_tags],
                                initializer=tf.zeros_initializer(),
                                dtype=tf.float32)
            s = tf.shape(output)
            output = tf.reshape(output, [-1, 2 * self.hidden_dim])
            pred = tf.matmul(output, W) + b
            self.logits = tf.reshape(pred, [-1, s[1], self.num_tags])

    def softmax_pred_op(self):
        if not self.useCRF:
            self.labels_softmax_ = tf.argmax(self.logits, axis=-1)
            self.labels_softmax_ = tf.cast(self.labels_softmax_, tf.int32)

    def loss_op(self):
        if self.useCRF:
            log_likelihood, self.transition_params = tf.contrib.crf.crf_log_likelihood(inputs=self.logits,
                                                                                       tag_indices=self.labels,
                                                                                       sequence_lengths=self.sequence_lengths)
            self.loss = -tf.reduce_mean(log_likelihood)
        else:
            losses = tf.nn.sparse_softmax_cross_entropy_with_logits(logits=self.logits,
                                                                    labels=self.labels)
            # shape = (batch, sentence, nclasses)
            mask = tf.sequence_mask(self.sequence_lengths)
            # apply mask
            losses = tf.boolean_mask(losses, mask)
            self.loss = tf.reduce_mean(losses)
        tf.summary.scalar("loss", self.loss)

    def train_step_op(self):
        with tf.variable_scope("train_step"):
            self.global_step = tf.Variable(0, name="global_step", trainable=False)
            if self.optimizer == 'Adam':
                optimizer = tf.train.AdamOptimizer(learning_rate=self.lr_pl)
            elif self.optimizer == 'Adadelta':
                optimizer = tf.train.AdadeltaOptimizer(learning_rate=self.lr_pl)
            elif self.optimizer == 'Adagrad':
                optimizer = tf.train.AdagradOptimizer(learning_rate=self.lr_pl)
            elif self.optimizer == 'RMSProp':
                optimizer = tf.train.RMSPropOptimizer(learning_rate=self.lr_pl)
            elif self.optimizer == 'Momentum':
                optimizer = tf.train.MomentumOptimizer(learning_rate=self.lr_pl, momentum=0.9)
            elif self.optimizer == 'SGD':
                optimizer = tf.train.GradientDescentOptimizer(learning_rate=self.lr_pl)
            else:
                optimizer = tf.train.GradientDescentOptimizer(learning_rate=self.lr_pl)
            self.train_op = optimizer.minimize(self.loss)

    def init_op(self):
        self.init_op = tf.global_variables_initializer()

    def add_summary(self, sess):
        self.merged = tf.summary.merge_all()
        self.file_writer = tf.summary.FileWriter(self.summary_path, sess.graph)

    def train(self, train, test):
        saver = tf.train.Saver(tf.global_variables())
        with tf.Session(config=self.config) as sess:
            sess.run(self.init_op)
            self.add_summary(sess)
            for epoch in range(self.epoch_num):
                self.run_one_epoch(sess, train, test, epoch, saver)

    def run_one_epoch(self, sess, train, test, epoch, saver):
        num_batches = (len(train) + self.batch_size - 1) // self.batch_size
        start_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        batches = batch_yield(train, self.batch_size)
        for step, (seqs, labels) in enumerate(batches):
            sys.stdout.write(' processing: {} batch / {} batches.'.format(step + 1, num_batches) + '\r')
            step_num = epoch * num_batches + step + 1
            _, loss_train, summary, step_num_ = sess.run([self.train_op, self.loss, self.merged, self.global_step],
                                                         feed_dict={self.word_ids: seqs, self.labels: labels,
                                                                    self.lr_pl: self.lr,
                                                                    self.dropout_pl: self.dropout_keep_prob,
                                                                    self.sequence_lengths: generate_sequence_len(
                                                                        self.max_len, self.batch_size)})
            if step + 1 == 1 or (step + 1) % 300 == 0 or step + 1 == num_batches:
                self.logger.info(
                    '{} epoch {}, step {}, loss: {:.4}, global_step: {}'.format(start_time, epoch + 1, step + 1,
                                                                                loss_train, step_num))

            self.file_writer.add_summary(summary, step_num)
            if epoch % 5 == 0:
                saver.save(sess, self.model_path, global_step=step_num)

        self.logger.info('===========validation / test===========')
        label_list_dev, seq_len_list_dev = self.dev_one_epoch(sess, test)
        self.evaluate(label_list_dev, test, epoch)

    def dev_one_epoch(self, sess, test):
        label_list, seq_len_list = [], []
        for seqs, labels in batch_yield(test, self.batch_size):
            label_list_, seq_len_list_ = self.predict_one_batch(sess, seqs, labels)
            label_list.extend(label_list_)
            seq_len_list.extend(seq_len_list_)
        return label_list, seq_len_list

    def predict_one_batch(self, sess, seqs, labels):
        seq_len_list = generate_sequence_len(self.max_len, self.batch_size)
        feed_dict = {self.word_ids: seqs, self.labels: labels,
                     self.lr_pl: self.lr,
                     self.dropout_pl: self.dropout_keep_prob,
                     self.sequence_lengths: seq_len_list}
        if self.useCRF:
            logits, transition_params = sess.run([self.logits, self.transition_params],
                                                 feed_dict=feed_dict)
            label_list = []
            for logit, seq_len in zip(logits, seq_len_list):
                viterbi_seq, _ = tf.contrib.crf.viterbi_decode(logit[:seq_len], transition_params)
                label_list.append(viterbi_seq)
            return label_list, seq_len_list
        else:
            label_list = sess.run(self.labels_softmax_, feed_dict=feed_dict)
            return label_list, seq_len_list

    def evaluate(self, label_list_dev, test, epoch=None):
        self.logger.info('===========正在进行评价===========')
        model_predict = []
        for label_, (sent, tag) in zip(label_list_dev, test):
            sent_res = []
            if len(label_) != len(sent):
                print(sent)
                print(len(label_))
                print(tag)
            for i in range(len(sent)):
                sent_res.append([sent[i], tag[i], label_[i]])
            model_predict.append(sent_res)
        epoch_num = str(epoch + 1) if epoch is not None else 'test'
        label_path = os.path.join(self.result_path, 'label_' + epoch_num)
        metric_path = os.path.join(self.result_path, 'result_metric_' + epoch_num)
        for _ in conlleval(model_predict, label_path, metric_path):
            self.logger.info(_)

    def test(self, test):
        saver = tf.train.Saver()
        with tf.Session(config=self.config) as sess:
            self.logger.info('=========== testing ===========')
            saver.restore(sess, self.model_path)
            label_list, seq_len_list = self.dev_one_epoch(sess, test)
            self.evaluate(label_list, test)
