<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- Favicons --%>
<link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
<link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.svg">
<link rel="apple-touch-icon" href="${pageContext.request.contextPath}/assets/images/favicon.svg">

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="contextPath" content="${pageContext.request.contextPath}">
<meta name="csrf-token" content="${csrfToken}">
<meta name="_csrf" content="${csrfToken}">
<meta name="theme-color" content="#2563eb">
<!-- Google Search Console Verification -->
<meta name="google-site-verification" content="pzfFJyk1bR6DXUw00hc_ffbD2amT6dgRCta6yGsyhKM" />

<%-- Dynamic SEO Meta Tags --%>
<c:choose>
    <c:when test="${not empty seo}">
        <title><c:out value="${seo.title}" /></title>
        <meta name="description" content="<c:out value='${seo.description}' />">
        <meta name="robots" content="<c:out value='${seo.robots}' />">
        <c:if test="${not empty seo.canonicalUrl}">
            <link rel="canonical" href="<c:out value='${seo.canonicalUrl}' />">
        </c:if>

        <%-- Open Graph / Facebook / WhatsApp --%>
        <meta property="og:site_name" content="ShopKart">
        <meta property="og:title" content="<c:out value='${seo.ogTitle}' />">
        <meta property="og:description" content="<c:out value='${seo.ogDescription}' />">
        <meta property="og:type" content="<c:out value='${seo.ogType}' />">
        <meta property="og:url" content="<c:out value='${seo.canonicalUrl}' />">
        <meta property="og:image" content="<c:out value='${seo.ogImage}' />">
        <meta property="og:locale" content="en_IN">

        <c:if test="${seo.ogType == 'product' && not empty seo.productPriceAmount}">
            <meta property="product:price:amount" content="<c:out value='${seo.productPriceAmount}' />">
            <meta property="product:price:currency" content="<c:out value='${seo.productPriceCurrency}' />">
            <c:if test="${not empty seo.productAvailability}">
                <meta property="product:availability" content="<c:out value='${seo.productAvailability}' />">
            </c:if>
        </c:if>

        <%-- Twitter / X Cards --%>
        <meta name="twitter:card" content="<c:out value='${seo.twitterCard}' />">
        <meta name="twitter:title" content="<c:out value='${seo.ogTitle}' />">
        <meta name="twitter:description" content="<c:out value='${seo.ogDescription}' />">
        <meta name="twitter:image" content="<c:out value='${seo.ogImage}' />">

        <%-- BreadcrumbList JSON-LD --%>
        <c:if test="${not empty seo.breadcrumbs && fn:length(seo.breadcrumbs) > 1}">
            <script type="application/ld+json">
            {
              "@context": "https://schema.org",
              "@type": "BreadcrumbList",
              "itemListElement": [
                <c:forEach items="${seo.breadcrumbs}" var="crumb" varStatus="loop">
                {
                  "@type": "ListItem",
                  "position": ${loop.index + 1},
                  "name": "<c:out value='${crumb.name}' />",
                  "item": "<c:out value='${crumb.url}' />"
                }<c:if test="${!loop.last}">,</c:if>
                </c:forEach>
              ]
            }
            </script>
        </c:if>
    </c:when>
    <c:otherwise>
        <%-- Fallback for unconfigured or private pages --%>
        <title>ShopKart | Premier Online Shopping India</title>
        <meta name="robots" content="noindex, nofollow">
    </c:otherwise>
</c:choose>
