@BodyProcessing
Feature: Body processing

This shows how particular tags are processed.

For details of how tags will look before and after for particular processing rules, see bodyprocessingrule.feature.

  Scenario Outline:
    Given a replacement tag <replacement> and the fastFt body contains <tagname> the transformer will TRANSFORM THE TAG

  Examples: Transform to html5-compliant tags
    | tagname | replacement |
    | b       | strong      |
    | i       | em          |
    | s       | del         |
    | strike  | del         |

  Scenario Outline:
    Given the WordPress body contains <tagname> the transformer will RETAIN ELEMENT AND REMOVE ATTRIBUTES

  Examples: Tidy up tags to remove attributes that probably relate to formatting
    | tagname |
    | strong  |
    | em      |
    | sub     |
    | sup     |
    | h1      |
    | h2      |
    | h3      |
    | h4      |
    | h5      |
    | h6      |
    | ol      |
    | ul      |
    | li      |
    | p       |
    | del     |
    | u       |

  Scenario Outline:
    Given the WordPress body contains <tagname> the transformer will STRIP ELEMENT AND CONTENTS

  Examples: Remove tags completely, including content, for html5 tags that we cannot support currently
    | tagname                    |
    | applet                     |
    | audio                      |
    | base                       |
    | basefont                   |
    | button                     |
    | canvas                     |
    | caption                    |
    | col                        |
    | colgroup                   |
    | command                    |
    | datalist                   |
    | dir                        |
    | embed                      |
    | fieldset                   |
    | form                       |
    | frame                      |
    | frameset                   |
    | head                       |
    | iframe                     |
    | input                      |
    | keygen                     |
    | label                      |
    | legend                     |
    | link                       |
    | map                        |
    | menu                       |
    | meta                       |
    | nav                        |
    | noframes                   |
    | noscript                   |
    | object                     |
    | optgroup                   |
    | option                     |
    | output                     |
    | param                      |
    | progress                   |
    | rp                         |
    | rt                         |
    | ruby                       |
    | script                     |
    | select                     |
    | source                     |
    | style                      |
    | table                      |
    | tbody                      |
    | td                         |
    | textarea                   |
    | tfoot                      |
    | th                         |
    | thead                      |
    | tr                         |
    | track                      |
    | video                      |
    | wbr                        |

  Scenario Outline:
    Given the WordPress body contains <tagname> the transformer will STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT

  Examples: Remove tag but leave any content - these are just some examples, by default anything not specified separately will be treated like this
    | tagname   | html                                               |
    | img       | <img src="abc.jpg"/>                               |
    | !--       | <!-- comments -->                                  |
    | weird     | <weird>text surrounded by unknown tags</weird>     |


  Scenario Outline: Remove inline images
    Given I have body <with inline images>
    When I transform it
    Then I get the body <without inline images>

  Examples:
    | with inline images                                                      | without inline images                                                    |
    | <p>Check this chart out!</p><p><a href="http://uat.ftalphaville.ft.com/files/2014/10/Chart5.png" target="_blank"><img class="aligncenter size-full wp-image-2012992" src="http://uat.ftalphaville.ft.com/files/2014/10/Chart5-e1413767777269.png" alt="" width="300" height="660" /></a></p><p>Profit!</p> | <p>Check this chart out!</p><p>Profit!</p> |


  Scenario Outline: Fix markup problems
    Given I have body <with errors>
    When I transform it
    Then I get the body <without errors>

  Examples:
    | with errors                                                       | without errors                                                    |
    | <p>a paragraph <span>with complex and incorrect</p> markup</span> | <p>a paragraph with complex and incorrect</p> markup        |
    | <p>a paragraph with <br> line break</p>                           | <p>a paragraph with <br/> line break</p>                          |
