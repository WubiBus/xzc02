package cn.incorner.contrast.data.entity;

import java.util.List;

/**
 * banner接口响应实体类
 * 
 * @author yeshimin
 */
public class BannerResultEntity {

	private String status;
	private List<BannerEntity> banners;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<BannerEntity> getBanners() {
		return banners;
	}

	public void setBanners(List<BannerEntity> banners) {
		this.banners = banners;
	}

}
