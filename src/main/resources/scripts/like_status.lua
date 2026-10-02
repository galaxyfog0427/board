-- KEYS[1] = 좋아요 Set
-- ARGV[1] = 조회하는 회원 id (비회원은 '0')
-- 반환: Set이 없으면 nil, 있으면 {좋아요 수, 내가 눌렀는지(1/0)}
if redis.call('EXISTS', KEYS[1]) == 0 then
    return {}
end
return { redis.call('SCARD', KEYS[1]) - 1, redis.call('SISMEMBER', KEYS[1], ARGV[1]) }