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

  Scenario Outline:
    Given the fastFt body contains <tagname> the transformer will RETAIN ELEMENT AND REMOVE ATTRIBUTES

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

  Scenario Outline:
    Given the fastFt body contains <tagname> the transformer will STRIP ELEMENT AND CONTENTS

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
    | del                        |
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
    | s                          |
    | script                     |
    | select                     |
    | source                     |
    | strike                     |
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
    Given the fastFt body contains <tagname> the transformer will STRIP ELEMENT AND LEAVE CONTENT BY DEFAULT

  Examples: Remove tag but leave any content - these are just some examples, by default anything not specified separately will be treated like this
    | tagname   | html                                               |
    | img       | <img src="abc.jpg"/>                               |
    | !--       | <!-- comments -->                                  |
    | weird     | <weird>text surrounded by unknown tags</weird>     |


  Scenario Outline: Fix markup problems
    Given I have html <with errors>
    When I transform it
    Then I get the html <without errors>

  Examples:
    | with errors                                                       | without errors                                                    |
    | <p>a paragraph <span>with complex and incorrect</p> markup</span> | <p>a paragraph <span>with complex and incorrect</span> markup</p> |
    | <p>a paragraph with <br> line break</p>                           | <p>a paragraph with <br/> line break</p>                          |
