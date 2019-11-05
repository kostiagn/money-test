@all
Feature: transaction tests

  @success
  Scenario: Init transaction is success
    Given Exists first account with balance 100.65
    And Exists second account with balance 66.77
    When Client initializes transaction from first account to second account with amount 33.99
    Then System responds with success code
    And Client sees the transaction with amount 33.99 and in status INIT

  @success
  Scenario: Client can see transaction info
    Given Exists first account with balance 100.65
    And Exists second account with balance 66.77
    When Client initializes transaction from first account to second account with amount 33.99
    And Client gets transaction
    Then System responds with success code
    And Client sees the transaction with amount 33.99 and in status INIT

  @success
  Scenario: Transfer is success
    Given Exists first account with balance 101.65
    And Exists second account with balance 7.66
    When Client initializes transaction from first account to second account with amount 44.88
    And Client commits transaction
    Then System responds with noContent code
    And Balance of the first account is 56.77
    And Balance of the second account is 52.54
    And Transaction has status SUCCESS

  @fail
  Scenario Outline: Init transaction - Error message
    Given Exists first account with balance <first acc balance> with status <first acc status>
    And Exists second account with balance 0 with status <second acc status>
    When Client initializes transaction from first account to second account with amount <transaction amount>
    Then System responds with expectationFailed code
    And Client sees error message <error message>
    Examples:
      | first acc status | first acc balance | second acc status | transaction amount | error message                                |
      | CLOSED           | 100               | OPEN              | 100                | account is not in open status                |
      | OPEN             | 100               | CLOSED            | 100                | account is not in open status                |
      | OPEN             | 100               | OPEN              | 100.01             | there is not enough money for transaction    |
      | OPEN             | 100               | OPEN              | 0                  | transaction amount must be greater then zero |
      | OPEN             | 100               | OPEN              | -0.01              | transaction amount must be greater then zero |

  @fail
  Scenario Outline: Commit transaction - Error message
    Given Exists first account with balance 100 with status OPEN
    And Exists second account with balance 0 with status OPEN
    And Client initializes transaction from first account to second account with amount 100
    And Balance was changed to <balance> and status was changed to <status> for <account> account
    When Client commits transaction
    Then System responds with expectationFailed code
    And Client sees error message <error message>
    And Transaction has status FAIL
    Examples:
      | account | balance | status | error message                             |
      | first   | 100     | CLOSED | account is not in open status             |
      | first   | 99.99   | OPEN   | there is not enough money for transaction |
      | second  | 100     | CLOSED | account is not in open status             |

  @fail
  Scenario: Commit transaction twice
    Given Exists first account with balance 100 with status OPEN
    And Exists second account with balance 0 with status OPEN
    And Client initializes transaction from first account to second account with amount 100
    And Client commits transaction
    When Client commits transaction
    Then System responds with expectationFailed code
    And Client sees error message transaction has closed yet
