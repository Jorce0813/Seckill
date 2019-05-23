<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>秒杀详情页</title>
    <!-- 了解静态包含跟动态包含的区别 -->
    <%@include file="common/head.jsp" %>
</head>
<body>
    <div class="container">
        <div class = "panel panel-info">
            <div class="panel panel-default text-center">
                <div class="panel-heading"><h2>${seckill.name}</h2></div>
            </div>
            <div class="panel-body text-center">
                <h2 class = "text-danger">
                    <!-- 显示time图标 -->
                    <span class="glyphicon glyphicon-time"></span>
                    <!-- 展示倒计时 -->
                    <span class="glyphicon" id="seckillBox"></span>
                </h2>
            </div>
      </div>
    </div>

    <!-- 登录弹出层，输入电话 -->
    <div id="killPhoneModal" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title text-center">
                        <span class="glyphicon glyphicon-phone"></span>秒杀电话：
                    </h3>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-8 col-xs-offset-2">
                            <input type="text" name="killphone" id="killphoneKey"
                                   placeholder="填手机号^O^" class="form-control" />
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <span id="killphoneMessage" class="glyphicon"></span>
                    <button type="button" id="killPhoneBtn" class="btn btn-success">
                        <span class="glyphicon glyphicon-phone"></span> Submit
                    </button>
                </div>
            </div>
        </div>
    </div>
</body>
<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="https://cdn.staticfile.org/jquery/2.1.1/jquery.min.js"></script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
<!-- jQuery cookie操作插件 -->
<script src="https://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<!-- jQury countDonw倒计时插件  -->
<script src="https://cdn.bootcss.com/jquery.countdown/2.1.0/jquery.countdown.js"></script>
<!-- 引入自定义的JS文件 -->
<script src="/resouces/script/seckill.js" type="text/javascript"></script>
<!-- 调用JS文件的交互内容 -->
<script type="text/javascript">
    $(function(){
        //使用EL表达式传入参数
        seckill.detail.init({
            seckillId : ${seckill.seckillId},
            startTime : ${seckill.startTime.time},//毫秒
            endTime : ${seckill.endTime.time}
        });
    });
</script>

</html>
