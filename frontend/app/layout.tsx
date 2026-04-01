import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "간식 뭐먹지? - Snack Overflow",
  description: "반 구성원의 과자 선호를 공유하는 서비스, SNACK OVERFLOW",
  icons: {
    icon: "/favicon.svg",
    apple: "/favicon.png",
  },
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
