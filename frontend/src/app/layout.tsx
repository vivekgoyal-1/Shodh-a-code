import type { Metadata } from 'next'
import './globals.css'
export const metadata: Metadata = { title: 'Shodh-a-Code Contest', description: 'Live coding contest platform' }
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return <html lang="en"><body>{children}</body></html>
}
