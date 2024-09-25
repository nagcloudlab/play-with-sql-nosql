create keyspace

```cql
CREATE KEYSPACE IF NOT EXISTS mykeyspace
WITH REPLICATION = {
  'class' : 'SimpleStrategy',
  'replication_factor' : 1
};
describe keyspaces;
use mykeyspace;
```

create table

```cql
CREATE TABLE IF NOT EXISTS users (
  user_id UUID PRIMARY KEY,
  user_name TEXT,
  user_bcity TEXT
);
describe tables;
```

insert data

```cql
INSERT INTO users (user_id, user_name, user_bcity) VALUES (uuid(), 'John Doe', 'New York');
INSERT INTO users (user_id, user_name, user_bcity) VALUES (uuid(), 'Jane Doe', 'Los Angeles');
SELECT * FROM users;
```

```cql
SELECT token(user_id), user_id, user_name, user_bcity FROM users;
```

load csv

```cql
COPY hotel.available_rooms_by_hotel_date (hotel_id, date, room_number, is_available) FROM '/Users/nag/play-with-sql-nosql/available_rooms.csv' WITH HEADER = TRUE;
```

```cql
SELECT * FROM hotel.available_rooms_by_hotel_date;
```
