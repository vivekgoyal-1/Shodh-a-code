'use client'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
export default function Home() {
  const [contestId, setContestId] = useState('contest-1')
  const [username, setUsername] = useState('')
  const router = useRouter()
  const handleJoin = (e: React.FormEvent) => {
    e.preventDefault()
    if (contestId && username) {
      localStorage.setItem('username', username)
      router.push(`/contest/${contestId}`)
    }
  }
  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
        <h1 className="text-3xl font-bold text-center mb-6 text-indigo-600">Shodh-a-Code</h1>
        <p className="text-gray-600 text-center mb-6">Join a coding contest and compete!</p>
        <form onSubmit={handleJoin} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Contest ID</label>
            <input type="text" value={contestId} onChange={(e) => setContestId(e.target.value)} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" placeholder="Enter contest ID" required />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Username</label>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent" placeholder="Enter your username" required />
          </div>
          <button type="submit" className="w-full bg-indigo-600 text-white py-3 rounded-lg hover:bg-indigo-700 transition-colors font-medium">Join Contest</button>
        </form>
        <div className="mt-6 p-4 bg-gray-50 rounded-lg">
          <p className="text-sm text-gray-600"><strong>Sample Contest ID:</strong> contest-1</p>
        </div>
      </div>
    </main>
  )
}
