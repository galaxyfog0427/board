-- KEYS[1] = 좋아요 Set
-- ARGV[1] = TTL(초), ARGV[2] = 센티넬, ARGV[3..] = 좋아요한 회원 id
redis.call('SADD', KEYS[1], unpack(ARGV, 2))
redis.call('EXPIRE', KEYS[1], ARGV[1])