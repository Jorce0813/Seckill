//存放交互逻辑的JS代码
//javascript模块化
var seckill = {
    //封装秒杀相关的Ajax的url
    URL: {
        now: function () {
            return '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return '/seckill/' + seckillId + '/seckillExposer';
        },
        execution: function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },

    //处理秒杀-->获取秒杀接口,控制逻辑,执行秒杀
    handleSeckill: function (sekillId, node) { //node节点是前端面板的显示内容的区域
        node.hide().html('<button class = "btn btn-primary btn-lg" id = "killBtn" >开始秒杀</button>');
        $.post(seckill.URL.exposer(sekillId), {}, function (result) {
            //在回调函数中执行交互流程
            if (result && result['success']) {
                var exposer = result['data'];
                //秒杀开启,之所以在倒计时结束后进入秒杀流程还要判断秒杀是否开启是
                //因为客户端的时间个服务器端的时间不一致,在下面流程中会重新刷新倒计时
                if (exposer['exposed']) {
                    //秒杀开启,开始秒杀业务逻辑
                    //1. 获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(sekillId, md5);
                    //console.log(killUrl); //打印秒杀地址
                    //2. 绑定按钮事件
                    //通过.one('click')绑定一次点击事件,防止用户连续点击按钮向服务器端发送n次秒杀请求造成服务器处理压力
                    $('#killBtn').one('click', function () {
                        //1. 先将秒杀按钮置灰
                        $(this).addClass('disabled');
                        //2. 发送秒杀请求并回调函数处理结果
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //3.显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            } else {
                                console.log('result ' + result);
                            }
                        });
                    });
                    //将隐藏的node节点显示出来
                    node.show();

                } else {
                    //服务器端时间未到,秒杀未开启,重新执行countdown()函数
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countdown(sekillId, now, start, end);
                }
            } else {
                console.log('result ' + result);
            }
        });
    },

    //验证手机号
    validatedPhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },

    //计时交互
    countdown: function (seckillId, startTime, endTime, nowTime) {
        var seckillBox = $('#seckillBox');
        if (nowTime > endTime) {
            //秒杀结束
            seckillBox.html('秒杀结束!')
        } else if (nowTime < startTime) {
            //秒杀未开启
            var killTime = new Date(startTime + 1000); //秒杀开启时间+1000ms调整客户端的时间偏移
            seckillBox.countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒');
                seckillBox.html(format); //显示秒杀倒计时信息
                /* 调用.on()方法处理计时完成时的函数回调功能 */
            }).on('finish.countdown', function () {
                seckill.handleSeckill(seckillId, seckillBox);
            })
        } else {
            //秒杀进行中
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },

    //详情页交互的秒杀逻辑
    detail: {
        init: function (params) {
            //手机验证和登录,计时交互
            //规划交互流程
            //在Cookie中查找登录的电话号码killPhone
            var killPhone = $.cookie('killPhone');

            //验证用户是否使用了手机号登录
            //若用户执行秒杀前还没有登录,调用弹出框填写手机号
            if (!seckill.validatedPhone(killPhone)) {
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出框
                killPhoneModal.modal({
                    show: true, //显示弹出层
                    backdrop: 'static', //禁止位置关闭,即避免用户点击非弹出框位置而关闭
                    keyboard: false //关闭键盘；事件,即防止用户操作键盘而关闭弹出框
                });
                //绑定点击按钮事件
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killphoneKey').val();
                    console.log('inputPhone=' + inputPhone);//TODO
                    //验证用户输入的号码是否符合规则
                    if (seckill.validatedPhone(inputPhone)) {
                        //将电话号码存入Cookie中并设定存活时间为7天 && 存活路径在/seckill下
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        //刷新秒杀详情页,会重新执行init()方法
                        window.location.reload();
                    } else {
                        $('#killphoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }

            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            //经过上述的流程判断后,用户已经登录成功
            //下面开始计时交互流程
            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    var nowTime = result['data'];
                    //时间判断,计时交互
                    seckill.countdown(seckillId, startTime, endTime, nowTime);
                } else {
                    console.log('result' + result);
                }
            });
        }

    }
}