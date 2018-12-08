import argparse
import logging


def get_logger(filename):
    logger = logging.getLogger('logger')
    logger.setLevel(logging.DEBUG)
    logging.basicConfig(format='%(message)s', level=logging.DEBUG)
    handler = logging.FileHandler(filename)
    handler.setLevel(logging.DEBUG)
    handler.setFormatter(logging.Formatter('%(asctime)s:%(levelname)s: %(message)s'))
    logging.getLogger().addHandler(handler)
    return logger


def str2bool(v):
    # copy from StackOverflow
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')


def generate_sequence_len(max_len, batch_size):
    sequence_len = []
    for i in range(batch_size):
        sequence_len.append(max_len)
    return sequence_len


def conlleval(label_predict, label_path, metric_path):
    b_correct = 0
    e_correct = 0
    m_correct = 0
    s_correct = 0
    b_total = 0
    e_total = 0
    m_total = 0
    s_total = 0
    with open(label_path, "w") as fw:
        line = []
        for sent_result in label_predict:
            for char, tag, tag_ in sent_result:
                if char == '0':
                    continue
                else:
                    if tag == 0:
                        b_total += 1
                        if tag_ == tag:
                            b_correct += 1
                    elif tag == 1:
                        e_total += 1
                        if tag_ == tag:
                            e_correct += 1
                    elif tag == 2:
                        m_total += 1
                        if tag_ == tag:
                            m_correct += 1
                    elif tag == 3:
                        s_total += 1
                        if tag_ == tag:
                            s_correct += 1
                line.append("{} {} {}\n".format(char, tag, tag_))
            line.append("\n")
        fw.writelines(line)
    total = b_total + e_total + m_total + s_total
    correct = b_correct + e_correct + m_correct + s_correct
    metrics = ["测试的字数为{}，其中分词正确的字数为{}，准确率为{}".format(total, correct, correct / total),
               "B的字数为{}，其中B被正确预测的字数为{}，准确率为{}".format(b_total, b_correct, b_correct / b_total),
               "E的字数为{}，其中E被正确预测的字数为{}，准确率为{}".format(e_total, e_correct, e_correct / e_total),
               "M的字数为{}，其中M被正确预测的字数为{}，准确率为{}".format(m_total, m_correct, m_correct / m_total),
               "S的字数为{}，其中S被正确预测的字数为{}，准确率为{}".format(s_total, s_correct, s_correct / s_total)]
    with open(metric_path, "w") as fw:
        fw.writelines(metrics)
    return metrics
