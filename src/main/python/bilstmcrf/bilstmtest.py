import tensorflow as tf

inputs = []
cell_fw = tf.contrib.rnn.LSTMCell(num_units=100)
cell_bw = tf.contrib.rnn.LSTMCell(num_units=100)

(outputs, output_states) = tf.nn.bidirectional_dynamic_rnn(cell_fw, cell_bw, inputs, sequence_length=300)
