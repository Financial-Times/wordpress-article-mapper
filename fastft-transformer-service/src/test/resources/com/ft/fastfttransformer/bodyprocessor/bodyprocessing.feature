@BodyProcessing
Feature: Body processing

  Scenario Outline:
    When the fastFt body contains <tagname>
    Then the element replaced with <replacement>

  Examples:
    | tagname | replacement |
    | b       | strong      |
    | i       | em          |

  Scenario Outline:
    When the fastFt body contains <tagname>
    Then the element is retained without attributes

  Examples: RetainElementsWithoutAttributes
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
    | p       |
    | ol      |
    | ul      |
    | li      |

  Scenario Outline:
    When the fastFt body contains <tagname>
    Then the element and contents are stripped

  Examples: StripElementAndContent
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
    When the fastFt body contains <tagname>
    Then the element is stripped but the contents remain

  Examples: Strip
    | tagname                                            |
    | <img src="abc.jpg"/>                               |
    | <!-- comments -->                                  |
    | <weird>text surrounded by unknown tags</weird>     |



