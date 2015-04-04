<?xml version="1.0" encoding="UTF-8"?>
<sld:NamedLayer xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
    <sld:Name>Default Styler</sld:Name>
    <sld:Title>Default Styler</sld:Title>
    <sld:Abstract></sld:Abstract>
    <sld:UserStyle>
        <sld:FeatureTypeStyle>
            <sld:FeatureTypeName>feature</sld:FeatureTypeName>
            <sld:Rule>
                <sld:Name>name</sld:Name>
                <sld:Abstract>Abstract</sld:Abstract>
                <sld:Title>title</sld:Title>
                <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>
                <sld:TextSymbolizer>
                    <sld:Label>
                        <ogc:PropertyName>AREANAME</ogc:PropertyName>
                    </sld:Label>
                    <sld:Font>
                        <sld:CssParameter name="font-family">
                            <ogc:Literal>Arial</ogc:Literal>
                        </sld:CssParameter>
                        <sld:CssParameter name="font-size">
                            <ogc:Literal>12.0</ogc:Literal>
                        </sld:CssParameter>
                        <sld:CssParameter name="font-style">
                            <ogc:Literal>normal</ogc:Literal>
                        </sld:CssParameter>
                        <sld:CssParameter name="font-weight">
                            <ogc:Literal>normal</ogc:Literal>
                        </sld:CssParameter>
                    </sld:Font>
                    <sld:Label>
                        <sld:LabelPlacement>
                            <sld:PointPlacement>
                                <sld:AnchorPoint>
                                    <sld:AnchorPointX>
                                        <ogc:Literal>0.0</ogc:Literal>
                                    </sld:AnchorPointX>
                                    <sld:AnchorPointY>
                                        <ogc:Literal>0.0</ogc:Literal>
                                    </sld:AnchorPointY>
                                </sld:AnchorPoint>
                                <sld:Displacement>
                                    <sld:DisplacementX>
                                        <ogc:Literal>5.0</ogc:Literal>
                                    </sld:DisplacementX>
                                    <sld:DisplacementY>
                                        <ogc:Literal>-5.0</ogc:Literal>
                                    </sld:DisplacementY>
                                </sld:Displacement>
                                <sld:Rotation>
                                    <ogc:Literal>0.0</ogc:Literal>
                                </sld:Rotation>
                            </sld:PointPlacement>
                        </sld:LabelPlacement>
                    </sld:Label>
                    <sld:Halo>
                        <sld:Fill>
                            <sld:CssParameter name="fill">
                                <ogc:Literal>#000000</ogc:Literal>
                            </sld:CssParameter>
                            <sld:CssParameter name="fill-opacity">
                                <ogc:Literal>0.7</ogc:Literal>
                            </sld:CssParameter>
                        </sld:Fill>
                        <sld:Radius>
                            <ogc:Literal>2.2</ogc:Literal>
                        </sld:Radius>
                    </sld:Halo>
                    <sld:Fill>
                        <sld:CssParameter name="fill">
                            <ogc:Literal>#F0F0F0</ogc:Literal>
                        </sld:CssParameter>
                        <sld:CssParameter name="fill-opacity">
                            <ogc:Literal>1.0</ogc:Literal>
                        </sld:CssParameter>
                    </sld:Fill>
                </sld:TextSymbolizer>
            </sld:Rule>
        </sld:FeatureTypeStyle>
    </sld:UserStyle>
</sld:NamedLayer>
