package com.javalab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * [static 전역변수]
 * JDBC 프로그래밍을 위한 요소들을 모두 멤버변수 즉, 필드 위치로 뽑아올림.
 * - 본 클래스 어디에서라도 사용가능한 전역변수가 됨.
 *  [모듈화]
 *  - 데이터베이스 커넥션 + PreparedStatement + 쿼리실행 작업 모듈
 *  - 실제로 쿼리를 실행하고 결과를 받아오는 부분 모듈
 *  [미션]
 *  - 전체 상품의 정보를 조회하세요.(카테고리명이 나오도록)
 */
public class PointMain {
	// [멤버 변수]
	// 1. oracle 드라이버 이름 문자열 상수
	public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	// 2. oracle 데이터베이스 접속 경로(url) 문자열 상수
	public static final String DB_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";

	// 3. 데이터베이스 전송 객체
	public static Connection con = null;

	// 4. query 실행 객체
	public static PreparedStatement pstmt = null;

	// 5. select 결과 저장 객체
	public static ResultSet rs = null;

	// 6. oracle 계정(id/pwd)
	public static String oracleId = "tempdb";
	public static String oraclePwd = "1234";

	// main 메소드
	public static void main(String[] args) {

		// 1. 디비 접속 메소드 호출
		connectDB();

		// 2. 회원들과 보유 포인트 정보 조회
//		getMemberAndPoint();

		// 3. 이소미 회원에게 포인트 15점 추가 지급
//		updatePointSomi();
		
		// 4. 관리자에게 포인트 30점 추가 지급
//		updatePointManager();

		// 5. 전체 회원 평균 포인트보다 작은 회원 목록 조회
//		getMembersLesssThanAvg();

		// 6. 자원반환
		closeResource();

		// 7. 자원반환
		closeResource( pstmt, rs);
	} // main e

	// 드라이버 로딩과 커넥션 객체 생성 메소드
	private static void connectDB() {
		try {
			// 1. 드라이버 로딩
			Class.forName(DRIVER_NAME);
			System.out.println("1.드라이버로드 성공");

			// 2.데이터베이스 커넥션(연결)
			con = DriverManager.getConnection(DB_URL, oracleId, oraclePwd);
			System.out.println("2.커넥션 객체 생성 성공");

		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로드 ERR : " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		}
	} // end connectDB()

	// 2. 회원, 보유 포인트 정보 조회
	private static void getMemberAndPoint() {
		try {
			String sql = "select m.user_id, m.name, m.pwd, m.email, m.phone,";
			sql += " decode(m.admin, 0, '일반사용자', 1, '관리자') admin,";
			sql += " p.point_id, p.points, to_char(p.reg_date, 'yyyy-mm-dd') reg_date";
			sql += " from member m left outer join point p on m.user_id = p.user_id";
			
			pstmt = con.prepareStatement(sql);
			System.out.println("회원, 포인트 정보 조회");
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				System.out.println(rs.getString("user_id") + "\t" +
								rs.getString("name") + "\t" +
								rs.getString("pwd") + "\t" +
								rs.getString("email") + "\t" +
								rs.getString("phone") + "\t" +
								rs.getString("admin") + "\t" +
								rs.getInt("point_id") + "\t" +
								rs.getInt("points") + "\t" +
								rs.getString("reg_date"));
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR!: " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method

	// 3. 이소미 회원에게 포인트 15점 추가 지급
	private static void updatePointSomi() {
		try {
			int intPoint = 15;
			String strName = "이소미";
			
			String sql = "update point set points = points + ?";
			sql += " where user_id = (select user_id";
			sql += " from member m";
			sql += " where m.name = ?)";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, intPoint);
			pstmt.setString(2, strName);
			
			int result = pstmt.executeUpdate();
			if (result > 0) {
				System.out.println("3. 수정 성공");
			} else {
				System.out.println("3. 수정 실패");
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR!: " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method

	// 4. 관리자에게 포인트 30점 추가 지급
	private static void updatePointManager() {
		try {
			int Point = 30;
			int admin = 1;
			
			String sql = "update point set points = points + ?";
			sql += " where user_id in (select user_id";
			sql += " from member m";
			sql += " where admin = ?)";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, Point);
			pstmt.setInt(2, admin);

			int result = pstmt.executeUpdate();
			
			if (result > 0) {
				System.out.println("4. 수정 성공");
			} else {
				System.out.println("4. 수정 실패");
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR!: " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method
	
	// 5. 전체 회원 평균 포인트보다 작은 회원 목록 조회
	private static void getMembersLesssThanAvg() {
		try {
			String sql = "select m.user_id, m.name,m.email, m.phone, decode(m.admin, 0, '일반사용자', 1, '관리자') admin, p.points, to_char(p.reg_date, 'yyyy-mm-dd') reg_date";
			sql += " from member m left outer join point p on m.user_id = p.user_id";
			sql += " where p.points < (select avg(p.points)";
			sql += " from point p)";
			
			pstmt = con.prepareStatement(sql);
			System.out.println("전체 회원 평균 포인트보다 작은 회원");
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				System.out.println(rs.getString("user_id") + "\t" +
								rs.getString("name") + "\t" +
								rs.getString("email") + "\t" +
								rs.getNString("phone") + "\t" +
								rs.getString("admin") + "\t" +
								rs.getInt("points") + "\t" +
								rs.getString("reg_date"));
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR!: " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method
	
	// 자원반납
	private static void closeResource() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			System.out.println("자원반납 ERR: " + e.getMessage());
		}
	} // end method

	// 자원반납 2
	private static void closeResource(PreparedStatement pstmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}catch (SQLException e) {
			System.out.println("자원반납 ERR: " + e.getMessage());
		}
	} // end method

} // class e
