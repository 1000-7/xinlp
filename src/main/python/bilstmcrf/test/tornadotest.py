# -*- coding:utf-8 -*-

from tornado.web import Application, RequestHandler
from tornado.ioloop import IOLoop
from tornado.httpserver import HTTPServer


class IndexHandler(RequestHandler):

    def get(self):
        # 获取get方式传递的参数
        username = self.get_query_argument("username")
        print(username)

    def post(self):
        # 获取post方式传递的参数
        username = self.get_body_argument("username")
        print(username)


if __name__ == "__main__":
    app = Application([(r"/", IndexHandler)])

    app.listen(8000)

    IOLoop.current().start()

# 网页运行时需要传入参数
# 192.168.11.79:8000/?username=123
