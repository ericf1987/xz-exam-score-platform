<!doctype html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="default.css">
    <title>统计平台管理 - 主页</title>
</head>
<body>

<h4>服务器列表</h4>

<table border="1">
    <thead>
    <tr>
        <td>地址</td>
        <td>端口</td>
        <td>状态</td>
        <td>正在统计项目</td>
    </tr>
    </thead>
    <tbody>
    <#list agents as agent>
    <tr>
        <td>${agent.host}</td>
        <td>${agent.port?string["0"]}</td>
        <td>${agent.status}</td>
        <td>
            <#if agent.activeProjects?size == 0>
                0
            <#else>
                <a href="aggregating?server=${agent.host}:${agent.port?string["0"]}">
                ${agent.activeProjects?size}
                </a>
            </#if>
        </td>
    </tr>
    </#list>
    </tbody>
</table>

</body>
</html>