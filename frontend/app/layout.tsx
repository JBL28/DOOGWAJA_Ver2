import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "두과자 - 과자 뭐먹지?",
  description: "반 구성원의 과자 선호를 공유하는 서비스, 두과자(DOGWAJA)",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
