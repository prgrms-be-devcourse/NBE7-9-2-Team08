'use client';

import type { RepositoryItem } from '@/types/community';
import { useRouter } from 'next/navigation'

export default function RepositoryCard({ item }: { item: RepositoryItem }) {

  const router = useRouter();

  return (
    <article className="bg-white border border-gray-200 rounded-2xl shadow-sm p-5 hover:shadow-md transition-all duration-200">
      {/* 사용자 정보 */}
      <div className="flex items-center mb-3">
        {item.userImage ? (
          <img
            src={item.userImage || "./userInit.png"} // 수정 : 이미지 경로 못 찾음
            alt={item.userName}
            className="w-10 h-10 rounded-full mr-3"
            onError={(e) => {
              e.currentTarget.onerror = null // 무한 루프 방지
              e.currentTarget.src = "./userInit.png" // 로딩 실패 시 기본 이미지로 교체
            }}
          />
        ) : (
          <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center text-gray-600 font-bold mr-3">
            {item.userName.charAt(0).toUpperCase()}
          </div>
        )}
        <div>
          <p className="font-semibold text-sm">{item.userName}</p>
          <p className="text-gray-500 text-xs">@{item.userName.toLowerCase()}</p>
        </div>
        <span className="ml-auto text-gray-400 text-xs">2시간 전</span>
      </div>

      {/* 레포지토리 링크 */}
      <a
        href="#"
        className="text-blue-600 font-semibold text-sm hover:underline"
      >
        {item.userName}/{item.repositoryName}
      </a>

      {/* 요약 */}
      <p className="mt-2 text-gray-700 text-sm leading-relaxed">
        {item.summary}
      </p>

      {/* 점수 */}
      <div className="bg-gray-50 mt-4 p-3 rounded-xl border">
        <p className="text-xs text-gray-500 mb-1 font-medium">Overall Score</p>
        <p className="text-green-600 font-bold text-2xl">{item.totalScore}</p>
      </div>

      {/* 언어 태그 */}
      <div className="mt-3 flex flex-wrap gap-2">
        {item.language.map((lang, idx) => (
          <span
            key={idx}
            className="text-xs bg-gray-100 text-gray-800 px-2 py-1 rounded-full font-medium"
          >
            {lang}
          </span>
        ))}
      </div>

      {/* 하단 */}
      <div className="flex justify-between items-center mt-4 text-gray-500 text-sm">

        <button
          className="border px-3 py-1 rounded-full text-xs font-semibold hover:bg-gray-100"
          type="button"
          onClick={() => router.push(`/analysis/${item.repositoryId}`) }
        >
          View Analysis
        </button>
      </div>
    </article>
  );
}
