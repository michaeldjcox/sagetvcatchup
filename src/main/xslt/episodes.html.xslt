<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <head>
                <title>Episodes</title>
                <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
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
                        <th>OrigAirDate</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>OrigAirTime</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>ServiceUrl</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>IconUrl</th>
                        <xsl:text>&#10;</xsl:text>
                        <th>PodcastUrl</th>
                    </tr>
                    <xsl:for-each
                            select="uk.co.mdjcox.sagetv.model.Catalog/episodes/entry/uk.co.mdjcox.sagetv.model.Episode">
                        <tr>
                            <td>
                                <xsl:value-of select="sourceId"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <a href="/episode?id={id};type=html">
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
                                <xsl:value-of select="podcastTitle"/>
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
                                <xsl:value-of select="origAirDate"/>
                            </td>
                            <xsl:text>&#10;</xsl:text>
                            <td>
                                <xsl:value-of select="origAirTime"/>
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
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>