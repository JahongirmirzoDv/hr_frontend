package uz.mobiledv.hr_frontend.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uz.mobiledv.hr_frontend.data.ApiService
import uz.mobiledv.hr_frontend.data.HrRepository
import uz.mobiledv.hr_frontend.data.storage.AuthStorage
import uz.mobiledv.hr_frontend.vm.AttendanceManagementViewModel
import uz.mobiledv.hr_frontend.vm.EmployeeManagementViewModel
import uz.mobiledv.hr_frontend.vm.ProjectManagementViewModel
import uz.mobiledv.hr_frontend.vm.ReportingDashboardViewModel
import uz.mobiledv.hr_frontend.vm.UserManagementViewModel

val appModule = module {
    single { ApiService() }
    single { AuthStorage() }
    single { HrRepository(get(), get()) }
    viewModel { UserManagementViewModel(get()) }
    viewModel { EmployeeManagementViewModel(get()) }
    viewModel { ProjectManagementViewModel(get()) }
    viewModel { AttendanceManagementViewModel(get()) }
    viewModel { ReportingDashboardViewModel(get()) }
}