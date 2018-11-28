package segment.crf.app;

import segment.crf.XinCRFSegment;

public class XinCRFApp {
    public static void main(String[] args) {
        XinCRFSegment xinCRFSegment = new XinCRFSegment();
        xinCRFSegment.viterbi("今天天气很好");
    }
}
