package kz.gov.rfs.entity;

public enum Role {
    USER,                    // Обычный пользователь
    ADMIN,                   // Полный доступ ко всему
    NEWS_MANAGER,           // Управление новостями и вакансиями
    PROCUREMENT_MANAGER,    // Управление закупками
    ABOUT_MANAGER,          // Управление информацией о предприятии
    SERVICES_MANAGER,       // Управление сервисами
    CONTACTS_MANAGER,       // Управление контактами
    HR_MANAGER              // Управление пользователями (отдел кадров)
}