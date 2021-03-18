CREATE TABLE IF NOT EXISTS history (
  id IDENTITY NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  epc TEXT NOT NULL,
  pin_id INT NOT NULL,
  pin_state BOOL,
  op_status TEXT,
  PRIMARY KEY(id)
);
