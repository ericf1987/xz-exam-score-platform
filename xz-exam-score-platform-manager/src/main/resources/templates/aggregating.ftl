<!doctype html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="default.css">
    <title>统计平台管理 - 正在统计</title>
</head>
<body>

<#if agent??>
<#list agent.activeProjects as project>
    <p>${project.projectId}</p>
</#list>
<#else>
<p>没有找到指定的服务器</p>
</#if>

</body>
</html>