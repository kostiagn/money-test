CREATE TYPE transaction_status AS ENUM ('INIT', 'SUCCESS', 'FAIL');
CREATE TYPE account_status AS ENUM ('OPEN','CLOSED');


CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    balance DECIMAL NOT NULL,
    status account_status NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL
);

CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    from_account_id BIGINT NOT NULL references account,
    to_account_id BIGINT NOT NULL references account,
    status transaction_status NOT NULL,
    amount DECIMAL NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL
);