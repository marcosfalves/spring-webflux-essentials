package academy.devdojo.webflux.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import java.util.stream.Collectors

@Table("app_user")
data class AppUser(
    @field:Id
    val id:Int,
    val name:String,
    private val username:String,
    private val password:String,
    val authorities:String

) : UserDetails {

    override fun getAuthorities(): Collection<out GrantedAuthority> {
        return Arrays.stream(authorities.split(",").toTypedArray())
            .map { role: String? -> SimpleGrantedAuthority(role) }
            .collect(Collectors.toList())
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

}
