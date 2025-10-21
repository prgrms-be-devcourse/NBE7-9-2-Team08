// 커뮤니티 사용자 분석 결과 상세 페이지, 댓글 기능 있어야 하는 곳
"use client"

import { useCommunity } from "@/hooks/community/useCommunity"
import RepoList from "@/components/community/RepoList"

export default function CommunityPage() {
  const { repos, loading, error } = useCommunity()

  if (loading) return <div>Loading...</div>
  if (error) return <div>에러가 발생했습니다 😢</div>

  return <RepoList repos={repos} />
}