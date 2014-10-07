<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <head>
                <title>Episodes</title>
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
                <h1>Episodes</h1>
                <table>
                    <tr>
                        <th>SourceId</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Id</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Channel</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>ProgrammeTitle</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Series</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>SeriesTitle</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Episode</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>EpisodeTitle</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>Description</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>PodcastTitle</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>AirDate</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>AirTime</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>ServiceUrl</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>IconUrl</th>
                    </tr>
                    <xsl:for-each
                            select="uk.co.mdjcox.sagetv.model.Catalog/episodes/entry/uk.co.mdjcox.sagetv.model.Episode">
                        <tr>
                            <td>
                                <xsl:value-of select="sourceId"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <a href="/episode={id}">
                                    <xsl:value-of select="id"/>
                                </a>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="channel"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="programmeTitle"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="series"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="seriesTitle"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="episode"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="episodeTitle"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="description"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="programmeTitle"/> - <xsl:value-of select="seriesTitle"/> - <xsl:value-of select="episodeTitle"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="airDate"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="airTime"/>
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
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>