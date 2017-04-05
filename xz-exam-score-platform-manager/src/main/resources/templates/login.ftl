<!doctype html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="default.css">
    <title>统计平台管理 - 登录</title>
</head>
<body>

<#if message??>
    <div class="message">${message}</div>
</#if>

<form action="/login" method="post">
    <p class="field"><label><span class="label">用户名</span><input type="text" name="username" id="username"></label></p>
    <p class="field"><label><span class="label">密码</span><input type="password" name="password" id="password"></label></p>
    <p class="field"><button type="submit">登录</button></p>
</form>

</body>
</html>