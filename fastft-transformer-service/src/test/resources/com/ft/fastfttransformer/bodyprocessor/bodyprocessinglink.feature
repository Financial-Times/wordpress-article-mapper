@BodyProcessing
Feature: Body processing links

#  separate scenarios of one generic scenario with internal/external/relative etc examples

  Scenario Outline:
    When I have <internal link html>
    Then it is left unmodified

  Examples:
  | internal link html                                                               |
  | <p>Good stories <a href="http://on.ft.com/1v6P55X">link to</a> other stories</p> |

  Scenario Outline:
    When I have <external link html>
    Then it is left unmodified

  Examples:
    | external link html                                                                                    |
    | <p>Better stories <a href="http://example.com/fascinating-insights.html">link to other</a> sites</p>  |
#
#  Scenario Outline:
#    When I have a relative link
#    Then it is retained as is
#
#  Examples:
#
#  Scenario Outline:
#    When I have a mailto link
#    Then
#
#  Examples:
#
#  Scenario Outline:
#    When I have an anchor
#    Then it is removed
#
#  Examples: