<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <xsl:for-each select="uk.co.mdjcox.sagetv.model.Episode">

        <html>
            <head>
                <title>Details page for <xsl:value-of select="programmeTitle"/> - <xsl:value-of select="seriesTitle"/> - <xsl:value-of select="episodeTitle"/></title>
                <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
            </head>
            <body>
                <h1><xsl:value-of select="programmeTitle"/> - <xsl:value-of select="seriesTitle"/> - <xsl:value-of select="episodeTitle"/></h1>
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
                        <td>Episode</td>
                    </tr>
                    <tr>
                        <td>Id</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="id"/></td>
                    </tr>
                    <tr>
                        <td>Channel</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="channel"/></td>
                    </tr>
                    <tr>
                        <td>ProgrammeTitle</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="programmeTitle"/></td>
                    </tr>
                    <tr>
                        <td>SeriesTitle</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="seriesTitle"/></td>
                    </tr>
                    <tr>
                        <td>EpisodeTitle</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="episodeTitle"/></td>
                    </tr>
                    <tr>
                        <td>Description</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="description"/></td>
                    </tr>
                    <tr>
                        <td>Series</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="series"/></td>
                    </tr>
                    <tr>
                        <td>Episode</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="episode"/></td>
                    </tr>
                    <tr>
                        <td>Genres</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><ul>
                            <xsl:for-each select="genres/string">
                            <li><xsl:value-of select="."/></li>
                            </xsl:for-each>
                        </ul></td>
                    </tr>
                    <tr>
                        <td>Date</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="airDate"/></td>
                    </tr>
                    <tr>
                        <td>Time</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="airTime"/></td>
                    </tr>
                    <tr>
                        <td>PodcastTitle</td>
                        <xsl:text>&#10;</xsl:text>
                        <td><xsl:value-of select="programmeTitle"/> - <xsl:value-of select="seriesTitle"/> - <xsl:value-of select="episodeTitle"/></td>
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