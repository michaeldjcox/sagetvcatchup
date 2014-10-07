<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <xsl:for-each select="uk.co.mdjcox.sagetv.model.Programme">

        <html>
            <head>
                <title>Details page for <xsl:value-of select="longName"/></title>
                <style>
table, th, td {
border: 1px solid black;
border-collapse: collapse;
}
th, td {
padding: 5px;
text-align: left;
}
table.names th	{
background-color: #c1c1c1;
}
</style>
                <style type="text/css">
BODY { color: #000000; background-color: white; font-family: Verdana; margin-left: 10px; margin-top: 0px; }
#content { margin-left: 10px; font-size: .70em; padding-bottom: 2em; }
A:link { color: #336699; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}
A:visited { color: #6699cc; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}
A:active { color: #336699; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}
A:hover { color: cc3300; font-weight: bold; text-decoration: underline; margin-left: -10px;padding-left: 15px;}
P { color: #000000; margin-top: 0px; margin-bottom: 12px; margin-left: -10px; font-family: Verdana; padding-left: 15px;}
pre { background-color: #e5e5cc; padding: 5px; font-family: Courier New; font-size: x-small; margin-top: -5px; border: 1px #f0f0e0 solid; }
td { color: #000000; font-family: Verdana; font-size: .7em; }
h1 { color: #ffffff; font-family: Tahoma; font-size: 26px; font-weight: normal; background-color: #003366; margin-top: 0px; margin-bottom: 0px; margin-left: -10px; padding-top: 10px; padding-bottom: 3px; padding-left: 15px; width: 105%; }
h2 { font-size: 1.5em; font-weight: bold; margin-top: 25px; margin-bottom: 10px; border-top: 1px solid #003366; margin-left: -10px; padding-left: 15px; color: #003366; }
h3 { font-size: 1.1em; color: #000000; margin-left: -10px; margin-top: 10px; margin-bottom: 10px; padding-left: 15px; }
ul { margin-top: 10px; margin-left: 20px; }
ol { margin-top: 10px; margin-left: 20px; }
li { margin-top: 10px; color: #000000; }
font.value { color: darkblue; font: bold; }
font.key { color: darkgreen; font: bold; }
font.error { color: darkred; font: bold; }
.heading1 { color: #ffffff; font-family: Tahoma; font-size: 26px; font-weight: normal; background-color: #003366; margin-top: 0px; margin-bottom: 0px; margin-left: 10px; padding-top: 10px; padding-bottom: 3px; padding-left: 15px; width: 105%; }
.button { background-color: #dcdcdc; font-family: Verdana; font-size: 1em; border-top: #cccccc 1px solid; border-bottom: #666666 1px solid; border-left: #cccccc 1px solid; border-right: #666666 1px solid; }
.frmheader { color: #000000; background: #dcdcdc; font-family: Verdana; font-size: .7em; font-weight: normal; border-bottom: 1px solid #dcdcdc; padding-top: 2px; padding-bottom: 2px; }
.frmtext { font-family: Verdana; font-size: .7em; margin-top: 8px; margin-bottom: 0px; margin-left: 10px; }
.frmInput { font-family: Verdana; font-size: 1em; }
.intro { margin-left: 10px; }
</style>
            </head>
            <body>
                <h1><xsl:value-of select="shortName"/></h1>
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
                    <tr>
                        <td>Episodes</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><ul>
                            <xsl:for-each select="episodes/string">
                                <li><a href="/episode={.}"><xsl:value-of select="."/></a></li>
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