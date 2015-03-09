@BodyProcessing
Feature: Body processing rules

This is an overview of how the various configuration rules work. 

For details of which rules apply for particular tags, see bodyprocessing.feature

  Scenario Outline:
    Given the tag <name> adheres to the <rule>
    When it is transformed, <before> becomes <after>

  Examples:
    | rule                                  | name      | before                                                | after             |
    | STRIP ELEMENT AND CONTENTS            | applet    | pretext <applet id="myApplet">Text</applet>posttext   | pretext posttext  |
    | STRIP ELEMENT AND LEAVE CONTENT       | unknown   | <unknown id="myUnknown">Some unknown text</unknown>   | Some unknown text |
    | RETAIN ELEMENT AND REMOVE ATTRIBUTES  | h1        | <h1 id="attr1" class="attr2">Text</h1>               | <h1>Text</h1>     |

  Scenario Outline: Transform one tag into another
    Given the before tag <beforename> and the after tag <aftername> adheres to the TRANSFORM THE TAG rule
    When it is transformed, <before> becomes <after>

  Examples:
    | beforename  | aftername | before                     | after                            |
    | b           | strong    | He said <b>what?</b>       | He said <strong>what?</strong>   |

  Scenario Outline: Convert HTML entities to unicode
    Given I have a rule to CONVERT HTML ENTITY TO UNICODE and an entity <entity>
    When it is transformed the entity <entity> should be replaced by the unicode codepoint <codepoint>

  Examples:
    | entity | codepoint |
    | &euro; | 0x20AC    |
    | &nbsp; | 0x00A0    |

  Scenario Outline: Remove empty paragraphs
    Given there are empty paragraphs in the body
    When it is transformed, <before> becomes <after>
    
  Examples: Remove empty paragraphs
    | before                                          | after                                  |
    | <p>Some text</p><p></p><p>Some more text</p>    | <p>Some text</p><p>Some more text</p>  |

  Scenario Outline: Handle videos
      Given I have text in Wordpress XML like <before>
      When I transform it into our Content Store format
      Then the body should be like <after>

  Examples:
    | before                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | after                                                                                                                                                                                                                                                                                                    |
    | <body><div data-asset-type="embed"><iframe src="//player.vimeo.com/video/107654482" width="500" height="281" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen><\/iframe><p><a href="http://vimeo.com/107654482">Kerrisdale Capital - October 6, 2014</a> from <a href="http://vimeo.com/kerrisdale">Kerrisdale Capital</a> on <a href="https://vimeo.com">Vimeo</a>.</p></div></body>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | <body><a data-asset-type="video" data-embedded="true" href="https://www.vimeo.com/107654482"></a><p><a href="http://vimeo.com/107654482">Kerrisdale Capital - October 6, 2014</a> from <a href="http://vimeo.com/kerrisdale">Kerrisdale Capital</a> on <a href="https://vimeo.com">Vimeo</a>.</p></body> |
    | <body><div class="video-container video-container-ftvideo" data-aspect-ratio="16:9"><div data-asset-type="video" data-asset-source="Brightcove" data-asset-ref="3655217965001"><object class="BrightcoveExperience" id="ft_video_54f496ba97863"><param name="bgcolor" value="#fff1e0"/><param name="width" value="590"/><param name="height" value="331"/><param name="wmode" value="transparent"/><param name="playerID" value="754609517001"/><param name="playerKey" value="AQ~~,AAAACxbljZk~,eD0zYozylZ0BsBE0lwVQCchDhI4xG0tl"/><param name="isVid" value="true"/><param name="isUI" value="true"/><param name="dynamicStreaming" value="true"/><param name="@videoPlayer" value="3655217965001"/><param name="linkBaseURL" value="http://video.ft.com/v/3655217965001"/><param name="includeAPI" value="true"/><param name="templateLoadHandler" value="onTemplateLoaded"/></object></div></div><div class="morevideo"><a href="http://video.ft.com/">More video</a></div></body>  | <body><a data-asset-type="video" data-embedded="true" href="http://video.ft.com/3655217965001"></a></body>                                                                                                                                                                                               |
    | <body><div class="video-container video-container-youtube" data-aspect-ratio="16:9"><div data-asset-type="video" data-asset-source="YouTube" data-asset-ref="fRqCVcSWbDc"><iframe width="590" height="331" src="http://www.youtube.com/embed/fRqCVcSWbDc?wmode=transparent" frameborder="0"></iframe></div></div></body>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | <body><a data-asset-type="video" data-embedded="true" href="https://www.youtube.com/watch?v=fRqCVcSWbDc"></a></body>                                                                                                                                                                                     |
    | <body><div data-asset-type="embed"><iframe width="560" height="315" src="http://www.youtube.com/embed/8LCofepdUzE" frameborder="0" allowfullscreen></iframe></div></body>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | <body><a data-asset-type="video" data-embedded="true" href="https://www.youtube.com/watch?v=8LCofepdUzE"></a></body>                                                                                                                                                                                     |
    

