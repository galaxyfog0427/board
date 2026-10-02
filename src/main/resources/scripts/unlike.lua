-- KEYS[1] = 좋아요 Set (board:post:{postId}:likes)
-- KEYS[2] = 좋아요를 눌렀던 날의 일별 랭킹 (board:ranking:likes:{yyyyMMdd})
-- ARGV[1] = memberId, ARGV[2] = postId
if redis.call('EXISTS', KEYS[1]) == 1 then
    redis.call('SREM', KEYS[1], ARGV[1])
end
if redis.call('EXISTS', KEYS[2]) == 1 then
    redis.call('ZINCRBY', KEYS[2], -1, ARGV[2])
end
return 1