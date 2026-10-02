-- KEYS[1] = 좋아요 Set (board:post:{postId}:likes)
-- KEYS[2] = 오늘 일별 랭킹 (board:ranking:likes:{yyyMMdd})
-- ARGV[1] = memberId, ARGV[2] = postId, ARGV[3] = 랭킹 키 TTL(초)
if redis.call('EXISTS', KEYS[1]) == 1 then
    redis.call('SADD', KEYS[1], ARGV[1])
end
redis.call('ZINCRBY', KEYS[2], 1, ARGV[2])
redis.call('EXPIRE', KEYS[2], ARGV[3], 'NX')
return 1