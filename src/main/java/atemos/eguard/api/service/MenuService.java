package atemos.eguard.api.service;

import atemos.eguard.api.dto.MenuDto;

import java.util.List;

/**
 * MenuService는 메뉴의 CRUD 기능을 제공하는 서비스 인터페이스입니다.
 * 이 인터페이스는 메뉴에 대한 CRUD 기능을 정의합니다.
 */
public interface MenuService {
    /**
     * 새로운 메뉴를 등록합니다.
     *
     * @param createMenuDto 메뉴를 생성하기 위한 데이터 전송 객체입니다.
     * @return 등록된 메뉴 정보를 담고 있는 객체입니다.
     */
    MenuDto.ReadMenuResponse create(MenuDto.CreateMenu createMenuDto);
    /**
     * 조건에 맞는 메뉴들을 조회합니다.
     * 시스템에 등록된 조건에 맞는 메뉴 정보를 조회합니다.
     *
     * @param readMenuRequestDto 메뉴 조회 조건을 담고 있는 객체입니다.
     * @return 조건에 맞는 메뉴 목록을 포함하는 응답 객체입니다.
     */
    List<MenuDto.ReadMenuResponse> read(MenuDto.ReadMenuRequest readMenuRequestDto);
    /**
     * 기존 메뉴를 수정합니다.
     *
     * @param menuId 수정할 메뉴의 ID입니다.
     * @param updateMenuDto 메뉴 수정을 위한 데이터 전송 객체입니다.
     * @return 수정된 메뉴 정보를 담고 있는 객체입니다.
     */
    MenuDto.ReadMenuResponse update(Long menuId, MenuDto.UpdateMenu updateMenuDto);
    /**
     * 기존 메뉴를 삭제합니다.
     *
     * @param menuId 삭제할 메뉴의 ID입니다.
     */
    void delete(Long menuId);
}