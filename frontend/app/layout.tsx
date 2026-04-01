import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "두과자 - 우리 방 과자 공유 서비스",
  description: "방 구성원의 과자 선호를 공유하는 서비스, 두과자(DOGWAJA)",
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
