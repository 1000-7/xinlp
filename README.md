# xinlp
学习《统计学习方法》，从第八章的EM算法到第十一章的CRF都基本实现了一遍 
## EM和GMM
先是学习了EM算法，实现了GMM高斯混合模型 \
高斯混合模型和kmeans很像，亲身测试男女身高这种事情GMM很难训练出来的 

## HMM和CRF
HMM 盒子与球问题 三种问题（概率，学习，预测）都实现了 \
CRF 人工定义特征函数太费劲了，其实就是特征工程，参数学习要用的方法也没实现。其实就是特征函数难定义。使用viterbi算法进行分词，学习借助
CRF，概率和hmm类似没有实现。
### 自己实现HMM分词和CRF分词
主要思想就是参数训练好的情况下（CRF++），viterbi算法实现就好。
HMM参数使用的python jieba分词的参数 \
CRF参照了Ansj和Hanlp两个的写法。 \
CRF参数来自于CRF++训练得到 
### 相关博客
http://www.unclewang.info/learn/machine-learning/730/ \
http://www.unclewang.info/learn/machine-learning/735/ 

## 自己实现Bi-LSTM+CRF分词
实现的有两个版本： \
ugly版本是第一遍直接实现的，因为以前也没怎么好好写过python，所以就随便命名、结构也很乱，做的时候不知道的东西就百度+bing去搜，反正遇山修路，过河修桥那样的实现的....,不过代码很精简，没有任何封装，看起来其实很流畅 \
非ugly版本是从github上找了一个很厉害的项目[guillaumegenthial/sequence_tagging](https://github.com/guillaumegenthial/sequence_tagging),仿照这种python代码完整度非常高的项目去重新写了一边代码（有很多地方直接抄的😊），代码很清晰，几个文件各司其职，也算没有辜负python（一个面向对象的动态解释型强语言）

### 相关博客
还没写，写了在添
