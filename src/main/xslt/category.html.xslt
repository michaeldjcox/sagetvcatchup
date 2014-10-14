<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <xsl:for-each select="uk.co.mdjcox.sagetv.model.Root|uk.co.mdjcox.sagetv.model.Source|uk.co.mdjcox.sagetv.model.SubCategory">

            <html>
                <head>
                    <title>Category: <xsl:value-of select="longName"/></title>
                    <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
                </head>
                <body>
                    <h1>Category: <xsl:value-of select="longName"/></h1>
                    <h2>Details</h2>
                    <table>
                        <tr>
                            <th>Field</th>
                            <xsl:text>&#10;</xsl:text>
                            <th>Value</th>
                        </tr>
                        <tr>
                            <td>SourceId</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><xsl:value-of select="sourceId"/></td>
                        </tr>
                        <tr>
                            <td>Type</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><xsl:value-of select="type"/></td>
                        </tr>
                        <tr>
                            <td>Id</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><xsl:value-of select="id"/></td>
                        </tr>
                        <tr>
                            <td>ParentId</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><xsl:value-of select="parentId"/></td>
                        </tr>
                        <tr>
                            <td>ShortName</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><xsl:value-of select="shortName"/></td>
                        </tr>
                        <tr>
                            <td>LongName</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><xsl:value-of select="longName"/></td>
                        </tr>
                        <tr>
                            <td>IconUrl</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><a href="{iconUrl}"><xsl:value-of select="iconUrl"/></a></td>
                        </tr>
                        <tr>
                            <td>ServiceUrl</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><a href="{serviceUrl}"><xsl:value-of select="serviceUrl"/></a></td>
                        </tr>
                        <tr>
                            <td>PodcastUrl</td>
                            <xsl:text>&#10;</xsl:text>
                            <xsl:if test="podcastUrl=''">
                                <td></td>
                            </xsl:if>
                            <xsl:if test="podcastUrl!=''">
                            <td><a href="{podcastUrl}"><xsl:value-of select="podcastUrl"/></a></td>
                            </xsl:if>
                        </tr>
                        <tr>
                            <td>MetaUrls</td>
                            <xsl:text>&#10;</xsl:text>
                            <td><ul>
                                <xsl:for-each select="metaUrls/string">
                                    <li><a href="{.}"><xsl:value-of select="."/></a></li>
                                </xsl:for-each>
                            </ul></td>
                        </tr>
                    </table>
                    <h2>Errors</h2>
                    <table>
                        <tr>
                            <th>Level</th>
                            <xsl:text>&#10;</xsl:text>
                            <th>Error</th>
                        </tr>
                        <xsl:for-each select="errors/uk.co.mdjcox.sagetv.model.ParseError">
                            <tr>
                                <td><xsl:value-of select="level"/></td>
                                <xsl:text>&#10;</xsl:text>
                                <td><xsl:value-of select="message"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </body>
            </html>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>