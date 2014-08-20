@BodyProcessing
Feature: Body processing rules

  Scenario Outline: Strip element and content
    Given I have start tag <start> and end tag <end>
    And the tag <name> adheres to the STRIP ELEMENT AND CONTENTS rule
    When I transform it
    Then the start tag <start> should have been removed
    And the end tag <end> should have be removed
    And the text inside should have been removed

  Examples:
    | name    | start                       | end          |
    | applet  | <applet id="myApplet">      | </applet>    |

  Scenario Outline: Strip element and leave content
    Given I have start tag <start> and end tag <end>
    And the tag <name> adheres to the STRIP ELEMENT AND LEAVE CONTENT rule
    When I transform it
    Then the start tag <start> should have been removed
    And the end tag <end> should have be removed
    And the text inside should not have been removed

  Examples:
    | name     | start                        | end           |
    | unknown  | <unknown id="myUnknown">     | </unknown>    |

  Scenario Outline: Retain element, remove attributes
    Given I have start tag <start> and end tag <end>
    And the tag <name> adheres to the RETAIN ELEMENT AND REMOVE ATTRIBUTES rule
    When I transform it
    Then the attributes inside the <start> tag should be removed
    And the text inside should not have been removed

  Examples:
    | name     | start                            | end           | after |
    | h1       | <h1 id="attr1" class="attr2">    | </h1>         | <h1>  |

  Scenario Outline: Transform one tag into another
    Given I have start tag <start> and end tag <end>
    And the before tag <name> and the after tag <aftername> adheres to the TRANSFORM THE TAG rule
    When I transform it
    Then the tag should be replaced with the tag <aftername>

  Examples:
    | name  | start     | end   | aftername   |
    | b     | <b>       | </b>  |  strong     |

  Scenario Outline: Convert HTML entities to unicode
    Given I have a rule to CONVERT HTML ENTITY TO UNICODE and an entity <entity>
    When I transform it
    Then the entity should be replaced by the unicode codepoint <codepoint>

  Examples:
    | entity | codepoint |
    | &euro; | 0x20AC    |
    | &nbsp; | 0x00A0    |


