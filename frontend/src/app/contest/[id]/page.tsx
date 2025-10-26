'use client'
import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import axios from 'axios'
import dynamic from 'next/dynamic'
const MonacoEditor = dynamic(() => import('@monaco-editor/react'), { ssr: false })
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'
interface Problem { id: string; title: string }
interface Contest { id: string; name: string; description: string; problems: Problem[] }
interface ProblemDetail { id: string; title: string; statement: string; inputFormat: string; outputFormat: string; sampleTestCases: Array<{ input: string; expectedOutput: string }> }
interface LeaderboardEntry { username: string; score: number; problemsSolved: number; rank: number }
export default function ContestPage() {
  const params = useParams()
  const contestId = params.id as string
  const [contest, setContest] = useState<Contest | null>(null)
  const [selectedProblem, setSelectedProblem] = useState<ProblemDetail | null>(null)
  const [code, setCode] = useState('// Write your code here')
  const [language, setLanguage] = useState('java')
  const [submissionStatus, setSubmissionStatus] = useState('')
  const [submissionId, setSubmissionId] = useState<number | null>(null)
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([])
  const [username, setUsername] = useState('')
  useEffect(() => {
    const stored = localStorage.getItem('username')
    if (stored) setUsername(stored)
    fetchContest()
    fetchLeaderboard()
    const leaderboardInterval = setInterval(fetchLeaderboard, 20000)
    return () => clearInterval(leaderboardInterval)
  }, [])
  useEffect(() => {
    if (submissionId) {
      const interval = setInterval(() => checkSubmissionStatus(submissionId), 2000)
      return () => clearInterval(interval)
    }
  }, [submissionId])
  const fetchContest = async () => {
    try {
      const res = await axios.get(`${API_URL}/contests/${contestId}`)
      setContest(res.data)
      if (res.data.problems.length > 0) fetchProblem(res.data.problems[0].id)
    } catch (err) { console.error('Error fetching contest:', err) }
  }
  const fetchProblem = async (problemId: string) => {
    try {
      const res = await axios.get(`${API_URL}/problems/${problemId}`)
      setSelectedProblem(res.data)
    } catch (err) { console.error('Error fetching problem:', err) }
  }
  const fetchLeaderboard = async () => {
    try {
      const res = await axios.get(`${API_URL}/contests/${contestId}/leaderboard`)
      setLeaderboard(res.data)
    } catch (err) { console.error('Error fetching leaderboard:', err) }
  }
  const handleSubmit = async () => {
    if (!selectedProblem || !username) return
    setSubmissionStatus('Submitting...')
    try {
      const res = await axios.post(`${API_URL}/submissions`, { username, contestId, problemId: selectedProblem.id, code, language })
      setSubmissionId(res.data.submissionId)
      setSubmissionStatus('Pending')
    } catch (err) { setSubmissionStatus('Error submitting'); console.error(err) }
  }
  const checkSubmissionStatus = async (id: number) => {
    try {
      const res = await axios.get(`${API_URL}/submissions/${id}`)
      setSubmissionStatus(res.data.status)
      if (['ACCEPTED', 'WRONG_ANSWER', 'RUNTIME_ERROR', 'TIME_LIMIT_EXCEEDED'].includes(res.data.status)) {
        setSubmissionId(null)
        fetchLeaderboard()
      }
    } catch (err) { console.error('Error checking status:', err) }
  }
  const getStatusColor = (status: string) => {
    switch(status) {
      case 'ACCEPTED': return 'text-green-600'
      case 'WRONG_ANSWER': return 'text-red-600'
      case 'RUNNING': case 'Pending': return 'text-yellow-600'
      default: return 'text-gray-600'
    }
  }
  if (!contest) return <div className="p-8">Loading...</div>
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-indigo-600 text-white p-4">
        <div className="container mx-auto">
          <h1 className="text-2xl font-bold">{contest.name}</h1>
          <p className="text-sm">User: {username}</p>
        </div>
      </header>
      <div className="container mx-auto p-4">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="bg-white rounded-lg shadow p-4">
            <h2 className="text-xl font-bold mb-4">Problems</h2>
            {contest.problems.map((problem) => (
              <button key={problem.id} onClick={() => fetchProblem(problem.id)} className={`w-full text-left p-3 rounded mb-2 transition ${selectedProblem?.id === problem.id ? 'bg-indigo-100 border-indigo-500 border-2' : 'bg-gray-50 hover:bg-gray-100'}`}>{problem.title}</button>
            ))}
            <div className="mt-6">
              <h3 className="font-bold mb-2">Leaderboard</h3>
              <div className="space-y-2">
                {leaderboard.slice(0, 10).map((entry) => (
                  <div key={entry.username} className="flex justify-between text-sm bg-gray-50 p-2 rounded">
                    <span>#{entry.rank} {entry.username}</span>
                    <span className="font-semibold">{entry.score}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="lg:col-span-2 space-y-4">
            {selectedProblem && (
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-2xl font-bold mb-4">{selectedProblem.title}</h2>
                <div className="prose max-w-none">
                  <p className="mb-4">{selectedProblem.statement}</p>
                  <h3 className="font-bold">Input Format</h3>
                  <p className="mb-4">{selectedProblem.inputFormat}</p>
                  <h3 className="font-bold">Output Format</h3>
                  <p className="mb-4">{selectedProblem.outputFormat}</p>
                  <h3 className="font-bold">Sample Test Cases</h3>
                  {selectedProblem.sampleTestCases.map((tc, idx) => (
                    <div key={idx} className="bg-gray-50 p-3 rounded mb-2">
                      <p><strong>Input:</strong></p>
                      <pre className="bg-gray-100 p-2 rounded">{tc.input}</pre>
                      <p className="mt-2"><strong>Output:</strong></p>
                      <pre className="bg-gray-100 p-2 rounded">{tc.expectedOutput}</pre>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <div className="bg-white rounded-lg shadow p-4">
              <div className="flex justify-between items-center mb-4">
                <h3 className="font-bold">Code Editor</h3>
                <select value={language} onChange={(e) => setLanguage(e.target.value)} className="border rounded px-3 py-1">
                  <option value="java">Java</option>
                  <option value="python">Python</option>
                  <option value="cpp">C++</option>
                </select>
              </div>
              <div className="border rounded overflow-hidden" style={{ height: '400px' }}>
                <MonacoEditor height="100%" language={language === 'cpp' ? 'cpp' : language} value={code} onChange={(value) => setCode(value || '')} theme="vs-light" options={{ minimap: { enabled: false }, fontSize: 14 }} />
              </div>
              <div className="mt-4 flex justify-between items-center">
                <button onClick={handleSubmit} className="bg-indigo-600 text-white px-6 py-2 rounded hover:bg-indigo-700 transition" disabled={!selectedProblem}>Submit Code</button>
                {submissionStatus && <div className={`font-semibold ${getStatusColor(submissionStatus)}`}>Status: {submissionStatus}</div>}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
