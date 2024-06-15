<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Enumeration"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>疑似リダイレクト画面</title>
</head>
<body>
<h1>疑似リダイレクト画面</h1>
<b>■セッション情報</b><br>
<%
Enumeration<String> attributeNames = session.getAttributeNames();
while (attributeNames.hasMoreElements()) {
    String name = attributeNames.nextElement();
    String value = String.valueOf(session.getAttribute(name));
    out.println(name + "=[" + value + "]<br>");
}
%>
<hr>
<b>■リクエストヘッダ情報</b><br>
<%
Enumeration<String> headerNames = request.getHeaderNames();
while (headerNames.hasMoreElements()) {
    String name = headerNames.nextElement();
    Enumeration<String> values = request.getHeaders(name);
    while (values.hasMoreElements()) {
        String value = values.nextElement();
        out.println(name + "=[" + value + "]<br>");
    }
}
%>
<hr>
<b>■パラメータ情報</b><br>
<%
Enumeration<String> parameterNames = request.getParameterNames();
while (parameterNames.hasMoreElements()) {
    String name = parameterNames.nextElement();
    String[] values = request.getParameterValues(name);
    while (String value : values) {
        out.println(name + "=[" + value + "]<br>");
    }
    
}
%>
</body>
</html>