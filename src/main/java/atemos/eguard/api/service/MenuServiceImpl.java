package atemos.eguard.api.service;

import atemos.eguard.api.dto.MenuDto;
import atemos.eguard.api.entity.Menu;
import atemos.eguard.api.repository.MenuRepository;
import atemos.eguard.api.specification.MenuSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MenuServiceImpl 클래스는 메뉴 관리 기능을 제공하는 서비스 클래스입니다.
 * 새로운 메뉴를 등록, 수정, 삭제하거나, 조건에 맞는 메뉴를 조회하는 기능을 수행합니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;

    /**
     * 새로운 메뉴를 등록합니다.
     *
     * @param createMenu 메뉴를 생성하기 위한 데이터 전송 객체입니다.
     * @return 등록된 메뉴 정보를 담고 있는 객체입니다.
     */
    @Override
    @Transactional
    public MenuDto.ReadMenuResponse create(MenuDto.CreateMenu createMenu) {
        // 상위 메뉴가 존재하는지 확인하여 depth 갱신
        var parentMenu = createMenu.getParentId() != null
                ? menuRepository.findById(createMenu.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상위 메뉴입니다."))
                : null;
        // 새로운 메뉴 객체 생성
        var menu = Menu.builder()
                .name(createMenu.getMenuName())
                .url(createMenu.getMenuUrl())
                .description(createMenu.getMenuDescription())
                .available(createMenu.getMenuAvailable())
                .accessibleRoles(createMenu.getAccessibleRoles())
                .depth(parentMenu != null ? parentMenu.getDepth() + 1 : 0)
                .parent(parentMenu)
                .build();
        // 메뉴 저장 및 저장된 메뉴 정보 반환
        var savedMenu = menuRepository.save(menu);
        return MenuDto.ReadMenuResponse.builder()
                .menuId(savedMenu.getId())
                .menuName(savedMenu.getName())
                .menuUrl(savedMenu.getUrl())
                .menuDescription(savedMenu.getDescription())
                .menuAvailable(savedMenu.getAvailable())
                .createdAt(savedMenu.getCreatedAt())
                .updatedAt(savedMenu.getUpdatedAt())
                .parentId(savedMenu.getParent() != null ? savedMenu.getParent().getId() : null)
                .menuDepth(savedMenu.getDepth())
                .accessibleRoles(savedMenu.getAccessibleRoles())
                .build();
    }

    /**
     * 조건에 맞는 메뉴들을 조회합니다.
     * 시스템에 등록된 조건에 맞는 메뉴 정보를 조회합니다.
     *
     * @param readMenuRequest 메뉴 조회 조건을 담고 있는 객체입니다.
     * @return 조건에 맞는 메뉴 목록을 포함하는 응답 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MenuDto.ReadMenuResponse> read(MenuDto.ReadMenuRequest readMenuRequest) {
        // 조건에 맞는 메뉴 목록을 조회하여 ID 오름차순으로 정렬
        var sortByIdAsc = Sort.by(Sort.Order.asc("id"));
        var menus = menuRepository.findAll(MenuSpecification.findWith(readMenuRequest), sortByIdAsc);
        // Parent ID를 기준으로 메뉴를 그룹화
        var groupedByParentId = menus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getParent() != null ? menu.getParent().getId() : -1L));
        // 루트 메뉴들 (Parent ID가 null인 메뉴들)
        var rootMenus = groupedByParentId.getOrDefault(-1L, new ArrayList<>()).stream()
                .map(menu -> MenuDto.ReadMenuResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .menuUrl(menu.getUrl())
                        .menuDescription(menu.getDescription())
                        .menuAvailable(menu.getAvailable())
                        .createdAt(menu.getCreatedAt())
                        .updatedAt(menu.getUpdatedAt())
                        .parentId(null)
                        .menuDepth(menu.getDepth())
                        .accessibleRoles(menu.getAccessibleRoles())
                        .build())
                .collect(Collectors.toList());
        // 각 루트 메뉴에 자식 메뉴들을 재귀적으로 설정
        for (var rootMenu : rootMenus) {
            setChildren(rootMenu, groupedByParentId);
        }
        return rootMenus;
    }

    /**
     * 기존 메뉴를 수정합니다.
     *
     * @param menuId 수정할 메뉴의 ID입니다.
     * @param updateMenu 메뉴 수정을 위한 데이터 전송 객체입니다.
     * @return 수정된 메뉴 정보를 담고 있는 객체입니다.
     */
    @Override
    @Transactional
    public MenuDto.ReadMenuResponse update(Long menuId, MenuDto.UpdateMenu updateMenu) {
        // 해당 메뉴가 존재하는지 확인
        var menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메뉴입니다."));
        // 상위 메뉴가 존재하는지 확인하고 설정 (없으면 현재 상위 메뉴 유지)
        if (updateMenu.getParentId() != null && !updateMenu.getParentId().equals(menu.getId())) {
            var parentMenu = menuRepository.findById(updateMenu.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상위 메뉴입니다."));
            menu.setParent(parentMenu);
            menu.setDepth(parentMenu.getDepth() + 1);
        } else if (updateMenu.getParentId() == null) {
            menu.setParent(null);
            menu.setDepth(0);  // 상위 메뉴가 없으면 루트 메뉴로 설정
        }
        // 메뉴 정보 업데이트
        Optional.ofNullable(updateMenu.getMenuName()).ifPresent(menu::setName);
        Optional.ofNullable(updateMenu.getMenuUrl()).ifPresent(menu::setUrl);
        Optional.ofNullable(updateMenu.getMenuDescription()).ifPresent(menu::setDescription);
        Optional.ofNullable(updateMenu.getMenuAvailable()).ifPresent(menu::setAvailable);
        Optional.ofNullable(updateMenu.getAccessibleRoles()).ifPresent(menu::setAccessibleRoles);
        // 엔티티 저장 후 리턴
        var updatedMenu = menuRepository.save(menu);
        return MenuDto.ReadMenuResponse.builder()
                .menuId(updatedMenu.getId())
                .menuName(updatedMenu.getName())
                .menuUrl(updatedMenu.getUrl())
                .menuDescription(updatedMenu.getDescription())
                .menuAvailable(updatedMenu.getAvailable())
                .createdAt(updatedMenu.getCreatedAt())
                .updatedAt(updatedMenu.getUpdatedAt())
                .parentId(updatedMenu.getParent() != null ? updatedMenu.getParent().getId() : null)
                .menuDepth(updatedMenu.getDepth())
                .accessibleRoles(updatedMenu.getAccessibleRoles())
                .build();
    }

    /**
     * 기존 메뉴를 삭제합니다.
     *
     * @param menuId 삭제할 메뉴의 ID입니다.
     */
    @Override
    @Transactional
    public void delete(Long menuId) {
        var menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메뉴입니다."));
        menuRepository.delete(menu);
    }

    /**
     * 루트 메뉴에 자식 메뉴를 설정하는 재귀 메서드입니다.
     *
     * @param parentMenu 부모 메뉴
     * @param groupedByParentId Parent ID로 그룹화된 메뉴 목록
     */
    private void setChildren(MenuDto.ReadMenuResponse parentMenu, Map<Long, List<Menu>> groupedByParentId) {
        // parentMenu의 ID를 기준으로 자식 메뉴 리스트를 가져와서 MenuDto.ReadMenuResponse로 변환
        var children = groupedByParentId.getOrDefault(parentMenu.getMenuId(), new ArrayList<>()).stream()
                .map(menu -> MenuDto.ReadMenuResponse.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .menuUrl(menu.getUrl())
                        .menuDescription(menu.getDescription())
                        .menuAvailable(menu.getAvailable())
                        .createdAt(menu.getCreatedAt())
                        .updatedAt(menu.getUpdatedAt())
                        .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                        .menuDepth(menu.getDepth())
                        .accessibleRoles(menu.getAccessibleRoles())
                        .build())
                .collect(Collectors.toList());
        // 부모 메뉴에 자식 메뉴 리스트를 설정
        parentMenu.setChildren(children);
        // 각 자식 메뉴에 대해 재귀적으로 setChildren을 호출하여 그 자식 메뉴들의 자식들도 설정
        for (var childMenu : children) {
            setChildren(childMenu, groupedByParentId);
        }
    }
}