@BodyProcessing
Feature: Body processing

  Scenario Outline:
    Given a replacement tag <replacement> and the fastFt body contains <tagname> the transformer will TRANSFORM THE TAG

  Examples:
    | tagname | replacement |
    | b       | strong      |
    | i       | em          |

  Scenario Outline:
    Given the fastFt body contains <tagname> the transformer will RETAIN ELEMENT AND REMOVE ATTRIBUTES

  Examples: Retain Elements Without Attributes
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

  Examples: Strip Element And Content
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
    Given the fastFt body contains <tagname> the transformer will STRIP ELEMENT AND CONTENTS BY DEFAULT

  Examples: Strip by default
    | tagname                                            |
    | <img src="abc.jpg"/>                               |
    | <!-- comments -->                                  |
    | <weird>text surrounded by unknown tags</weird>     |



