<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <head>
                <title>Programmes</title>
                <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
            </head>
            <body>
                <h1>Programmes</h1>
                <table>
                    <tr>
                        <th>SourceId</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>ParentId</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Type</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Id</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>ShortName</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>LongName</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>ServiceUrl</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>IconUrl</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>PodcastUrl</th>
                    </tr>
                    <xsl:for-each
                            select="catalog/programme">
                        <xsl:sort select="id"/>
                        <xsl:if test="parentId=''">
                            <tr>
                            <td>
                                <xsl:value-of select="sourceId"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="parentId"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="type"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <a href="/programme?id={id};type=html">
                                    <xsl:value-of select="id"/>
                                </a>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="shortName"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="longName"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <a href="{serviceUrl}">
                                    <xsl:value-of select="serviceUrl"/>
                                </a>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <a href="{iconUrl}">
                                    <xsl:value-of select="iconUrl"/>
                                </a>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <a href="{podcastUrl}">
                                    <xsl:value-of select="podcastUrl"/>
                                </a>
                            </td>
                        </tr>
                        </xsl:if>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>